package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.gui.Coloriser;
import org.workcraft.util.XmlUtil;


public class VisualGroup extends VisualTransformableNode {
	public static final int HIT_COMPONENT = 1;
	public static final int HIT_CONNECTION = 2;
	public static final int HIT_GROUP = 3;

	protected Set<VisualConnection> connections = new LinkedHashSet<VisualConnection>();
	protected Set<VisualComponent> components = new LinkedHashSet<VisualComponent>();
	protected Set<VisualGroup> groups = new LinkedHashSet<VisualGroup>();
	protected Set<VisualNode> children = new LinkedHashSet<VisualNode>();


	private void addPropertyChangeListener() {
		this.addListener(new PropertyChangeListener(){
			@Override
			public void propertyChanged(String propertyName, Object sender) {
				if(propertyName == "transform")
					for(VisualNode node : children)
					{
						if(node instanceof VisualTransformableNode)
							node.firePropertyChanged("transform");
					}

			}
		});
	}

	public VisualGroup () {
		super();
		addPropertyChangeListener();
	}

	public VisualGroup (Element element, Model model) throws VisualModelConstructionException {
		super(element);
		addPropertyChangeListener();

		List<Element> componentNodes = XmlUtil.getChildElements("component", element);

		for (Element vcompElement : componentNodes) {
			int ref = XmlUtil.readIntAttr(vcompElement, "ref", -1);
			Component refComponent = model.getMathModel().getComponentByID(ref);
			if (refComponent == null)
				throw new VisualModelConstructionException ("a visual component references to the model component with ID=" +
						vcompElement.getAttribute("ref")+", which was not found");
			VisualComponent visualComponent = PluginManager.createVisualComponent(refComponent, vcompElement);
			if (visualComponent == null)
				throw new VisualModelConstructionException ("a visual component references to the model component with ID=" +
						vcompElement.getAttribute("ref")+", which does not declare a visual represenation class");
			add(visualComponent);
			model.getVisualModel().addComponent(visualComponent);
		}

		List<Element> groupNodes = XmlUtil.getChildElements("group", element);

		for (Element groupElement : groupNodes) {
			VisualGroup visualGroup = new VisualGroup (groupElement, model);
			add (visualGroup);
			model.getVisualModel().addGroup(visualGroup);
		}

		List<Element> connectionNodes = XmlUtil.getChildElements("connection", element);

		for (Element vconElement : connectionNodes) {
			int ref = XmlUtil.readIntAttr(vconElement, "ref", -1);
			Connection refConnection = model.getMathModel().getConnectionByID(ref);
			VisualComponent first = model.getVisualModel().getComponentByRefID(refConnection.getFirst().getID());
			VisualComponent second = model.getVisualModel().getComponentByRefID(refConnection.getSecond().getID());
			if (refConnection == null)
				throw new VisualModelConstructionException ("a visual connection references to the model connection with ID=" +
						vconElement.getAttribute("ref")+", which was not found");
			VisualConnection visualConnection = PluginManager.createVisualConnection(refConnection, vconElement, first, second);
			if (visualConnection == null)
				throw new VisualModelConstructionException ("a visual connection references to the model connection with ID=" +
						vconElement.getAttribute("ref")+", which does not declare a visual represenation class");

			this.add(visualConnection);
			model.getVisualModel().addConnection(visualConnection);
		}
	}

	private void drawPreservingTransform(Graphics2D g, VisualNode nodeToDraw)
	{
		AffineTransform oldTransform = g.getTransform();
		nodeToDraw.draw(g);
		g.setTransform(oldTransform);
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g) {
		for (VisualConnection connection : connections)
			drawPreservingTransform(g, connection);

		for (VisualGroup group : groups)
			drawPreservingTransform(g, group);

		for (VisualComponent component : components)
			drawPreservingTransform(g, component);


		Rectangle2D bb = getBoundingBoxInLocalSpace();
		if (bb != null && parent != null) {
			g.setColor(Coloriser.colorise(Color.GRAY, colorisation));
			g.setStroke(new BasicStroke(0.02f));
			g.draw(getBoundingBoxInLocalSpace());
		}
	}

	public void add (VisualGroup group) {
		if (group.getParent()!=null)
			group.getParent().remove(group);
		groups.add(group);
		children.add(group);
		group.setParent(this);
	}

	public void add (VisualNode node) {
		if (node.getParent() == this)
			return;

		if (node.getParent() != null)
			node.getParent().remove(node);

		children.add(node);

		if (node instanceof VisualComponent)
			components.add((VisualComponent)node);
		else if (node instanceof VisualGroup)
			groups.add((VisualGroup)node);
		else if (node instanceof VisualConnection)
			connections.add((VisualConnection)node);
		else throw new UnsupportedOperationException("Unknown node type");

		node.setParent(this);
	}

	protected void remove (VisualNode node) {
		node.setParent(null);
		children.remove(node);

		if (node instanceof VisualComponent)
			components.remove((VisualComponent)node);
		else if (node instanceof VisualGroup)
			groups.remove((VisualGroup)node);
		else if (node instanceof VisualConnection)
			connections.remove((VisualConnection)node);
		else throw new UnsupportedOperationException("Unknown node type");
	}



	@Override
	public void toXML(Element groupElement) {
		super.toXML(groupElement);
		VisualModel.nodesToXml (groupElement, children);
	}

	public LinkedList<VisualNode> hitObjects(Rectangle2D rectInLocalSpace) {
		LinkedList<VisualNode> hit = new LinkedList<VisualNode>();

		for (VisualNode n : children) {
			Rectangle2D boundingBox = n.getBoundingBoxInParentSpace();
			if(boundingBox != null)
				if(rectInLocalSpace.contains(boundingBox))
					hit.add(n);
		}
		return hit;
	}

	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		VisualNode hit = hitNode(pointInLocalSpace);
		if (hitNode(pointInLocalSpace)==null)
			return 0;
		else
			if (hit instanceof VisualComponent)
				return 1;
			else if (hit instanceof VisualConnection)
				return 2;
			else if (hit instanceof VisualGroup)
				return 3;
		return 0;
	}

	private static <T> Iterable<T> reverse(Iterable<T> original)
	{
		final ArrayList<T> list = new ArrayList<T>();
		for (T node : original)
			list.add(node);
		return new Iterable<T>()
		{
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>()
				{
					private int cur = list.size();
					@Override
					public boolean hasNext() {
						return cur>0;
					}
					@Override
					public T next() {
						return list.get(--cur);
					}
					@Override
					public void remove() {
						throw new RuntimeException("Not supported");
					}
				};
			}
		};
	}

	private static <T extends VisualNode> Iterable<T> filterByBB(Iterable<T> nodes, final Point2D pointInLocalSpace) {
		return
			Filter.filter(nodes, new UnaryFunctor<T, Boolean>()
				{
					private static final long serialVersionUID = -7790168871113424836L;

					@Override
					public Boolean fn(T arg) {
						Rectangle2D boundingBox = arg.getBoundingBoxInParentSpace();
						return
							boundingBox != null &&
							boundingBox.contains(pointInLocalSpace);
					}
				}
		);
	}

	private static <T extends VisualNode> T hitVisualNode(Point2D pointInLocalSpace, Collection<T> nodes) {
		for (T node : reverse(filterByBB(nodes, pointInLocalSpace)))
			if (node.hitTestInParentSpace(pointInLocalSpace) != 0)
				return node;
		return null;
	}

	public VisualNode hitNode(Point2D pointInLocalSpace) {
		return hitVisualNode(pointInLocalSpace, children);
	}

	public VisualComponent hitComponent(Point2D pointInLocalSpace) {
		VisualComponent result = hitVisualNode(pointInLocalSpace, components);
		if(result!=null)
			return result;
		for (VisualGroup group : reverse(filterByBB(groups, pointInLocalSpace))) {
			Point2D pointInChildSpace = new Point2D.Double();
			group.parentToLocalTransform.transform(pointInLocalSpace, pointInChildSpace);
			result = group.hitComponent(pointInChildSpace);
			if(result!=null)
				return result;
		}
		return null;
	}

	private static Rectangle2D.Double mergeRect(Rectangle2D.Double rect, VisualNode node)
	{
		Rectangle2D addedRect = node.getBoundingBoxInParentSpace();

		if(addedRect == null)
			return rect;

		if(rect==null) {
			rect = new Rectangle2D.Double();
			rect.setRect(addedRect);
		}
		else
			rect.add(addedRect);

		return rect;
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D.Double rect = null;
		for(VisualComponent comp : components)
			rect = mergeRect(rect, comp);
		for(VisualGroup grp : groups)
			rect = mergeRect(rect, grp);
		return rect;
	}

	@Override
	public void clearColorisation() {
		colorisation = null;
		for (VisualNode node : children)
			node.clearColorisation();
	}

	@Override
	public void setColorisation(Color color) {
		colorisation = color;
		for (VisualNode node : children)
			node.setColorisation(color);
	}

	public List<VisualNode> unGroup() {
		ArrayList<VisualNode> result = new ArrayList<VisualNode>(children.size());
		for (VisualConnection connection : connections.toArray(new VisualConnection[connections.size()])) {
			parent.add(connection);
			result.add(connection);
		}

		try	{
			for (VisualTransformableNode group : groups.toArray(new VisualTransformableNode[groups.size()]))
			{
				parent.add(group);
				result.add(group);
				group.applyTransform(localToParentTransform);
			}
			for (VisualTransformableNode component : components.toArray(new VisualTransformableNode[components.size()]))
			{
				parent.add(component);
				result.add(component);
				component.applyTransform(localToParentTransform);
			}
		}
		catch(NoninvertibleTransformException ex) {
			throw new RuntimeException("localToParentTransform is not invertible!");
		}

		children.clear();
		groups.clear();
		components.clear();
		connections.clear();

		return result;
	}

	public final VisualNode[] getChildren() {
		return children.toArray(new VisualNode[0]);
	}

	public final VisualComponent[] getComponents() {
		return components.toArray(new VisualComponent[0]);
	}

	public final VisualConnection[] getConnections() {
		return connections.toArray(new VisualConnection[0]);
	}

	public final VisualGroup[] getGroups() {
		return groups.toArray(new VisualGroup[0]);
	}
}