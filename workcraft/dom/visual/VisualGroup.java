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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.ComponentFactory;
import org.workcraft.framework.ConnectionFactory;
import org.workcraft.framework.VisualNodeSerialiser;
import org.workcraft.framework.exceptions.VisualComponentCreationException;
import org.workcraft.framework.exceptions.VisualConnectionCreationException;
import org.workcraft.gui.Coloriser;
import org.workcraft.util.XmlUtil;


public class VisualGroup extends VisualTransformableNode {
	public static final int HIT_COMPONENT = 1;
	public static final int HIT_CONNECTION = 2;
	public static final int HIT_GROUP = 3;

	protected Set<VisualConnection> connections = new LinkedHashSet<VisualConnection>();
	protected Set<VisualComponent> components = new LinkedHashSet<VisualComponent>();
	protected Set<VisualGroup> groups = new LinkedHashSet<VisualGroup>();
	protected Set<VisualNode> misc = new LinkedHashSet<VisualNode>();
	protected Set<VisualNode> children = new LinkedHashSet<VisualNode>();

	private Element deferredGroupElement = null;
	private String label = "";

	private static <T extends VisualNode> Iterable<T> filterByBB(Iterable<T> nodes, final Point2D pointInLocalSpace) {
		return
			Filter.filter(nodes, new UnaryFunctor<T, Boolean>()
				{
					private static final long serialVersionUID = -7790168871113424836L;

					@Override
					public Boolean fn(T arg) {
						Rectangle2D boundingBox = arg.getBoundingBox();

						//System.out.println (boundingBox.toString());
						//System.out.println (pointInLocalSpace.toString());

						return
							boundingBox != null &&
							boundingBox.contains(pointInLocalSpace);
					}
				}
		);
	}

	private static <T extends VisualNode> T hitVisualNode(Point2D pointInLocalSpace, Collection<T> nodes) {
		for (T node : reverse(filterByBB(nodes, pointInLocalSpace)))
			if (node.hitTest(pointInLocalSpace) != null)
				return node;
		return null;
	}

	private static Rectangle2D.Double mergeRect(Rectangle2D.Double rect, VisualNode node)
	{
		Rectangle2D addedRect = node.getBoundingBox();

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

	private static <T> Iterable<T> reverse(Iterable<T> original)
	{
		final ArrayList<T> list = new ArrayList<T>();
		for (T node : original)
			list.add(node);
		return new Iterable<T>()
		{
			public Iterator<T> iterator() {
				return new Iterator<T>()
				{
					private int cur = list.size();
					public boolean hasNext() {
						return cur>0;
					}
					public T next() {
						return list.get(--cur);
					}
					public void remove() {
						throw new RuntimeException("Not supported");
					}
				};
			}
		};
	}

	public VisualGroup () {
		super();
		addXMLSerialisable();
		addPropertyChangeListener();
	}

	public VisualGroup (Element element, VisualModel model) throws VisualConnectionCreationException, VisualComponentCreationException {
		super(element);

		Element groupElement = XmlUtil.getChildElement(VisualGroup.class.getSimpleName(), element);
		label = XmlUtil.readStringAttr(groupElement, "label");

		addXMLSerialisable();
		addPropertyChangeListener();

		loadComponents(groupElement, model);
		loadSubgroups(groupElement, model);
		deferredGroupElement = groupElement;
	}

	private void addPropertyChangeListener() {
		this.addListener(new PropertyChangeListener(){
			public void onPropertyChanged(String propertyName, Object sender) {
				if(propertyName == "transform")
					for(VisualNode node : children)
					{
						if(node instanceof VisualTransformableNode)
							node.firePropertyChanged("transform");
					}

			}
		});
	}

	private void addXMLSerialisable() {
		addXMLSerialiser(new XMLSerialiser() {
			public String getTagName() {
				return VisualGroup.class.getSimpleName();
			}
			public void serialise(Element element) {
				XmlUtil.writeStringAttr(element, "label", label);
				VisualModel.nodesToXML (element, children);
			}
		});
	}

	private void drawPreservingTransform(Graphics2D g, VisualNode nodeToDraw)
	{
		AffineTransform oldTransform = g.getTransform();
		nodeToDraw.draw(g);
		g.setTransform(oldTransform);
	}

	private void loadComponents (Element groupElement, VisualModel model) throws VisualComponentCreationException {
		List<Element> componentNodes = XmlUtil.getChildElements("component", groupElement);

		for (Element vcompElement : componentNodes) {

			int ref = XmlUtil.readIntAttr(vcompElement, "ref", -1);

			Component refComponent = model.getMathModel().getComponentByRenamedID(ref);
			if (refComponent == null)
				throw new VisualComponentCreationException ("a visual component references to the model component with ID=" +
						vcompElement.getAttribute("ref") + " which was not found");

			VisualNode visualComponent = ComponentFactory.createVisualComponent(vcompElement, model);
			add(visualComponent);
			model.addComponents(visualComponent);
		}
	}

	private void loadConnections (Element groupElement, VisualModel model) throws VisualConnectionCreationException {
		List<Element> connectionNodes = XmlUtil.getChildElements("connection", groupElement);

		for (Element vconElement : connectionNodes) {
			VisualConnection visualConnection = ConnectionFactory.createVisualConnection(vconElement, model.getReferenceResolver());
			add(visualConnection);
			model.addConnection(visualConnection);
		}
	}

	private void loadSubgroups (Element groupElement, VisualModel model) throws VisualConnectionCreationException, VisualComponentCreationException {
		List<Element> groupNodes = XmlUtil.getChildElements("group", groupElement);

		for (Element subgroupElement : groupNodes) {
			VisualGroup visualGroup = new VisualGroup (subgroupElement, model);
			add (visualGroup);
		}
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g) {
		for (VisualConnection connection : connections)
			drawPreservingTransform(g, connection);

		for (VisualGroup group : groups)
			drawPreservingTransform(g, group);

		for (VisualComponent component : components)
			drawPreservingTransform(g, component);

		for (VisualNode node : misc)
			drawPreservingTransform(g, node);


		Rectangle2D bb = getBoundingBoxInLocalSpace();

		if (bb != null && getParent() != null) {
			bb.setRect(bb.getX() - 0.1, bb.getY() - 0.1, bb.getWidth() + 0.2, bb.getHeight() + 0.2);
			g.setColor(Coloriser.colorise(Color.GRAY, getColorisation()));
			g.setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{0.2f, 0.2f}, 0.0f));
			g.draw(bb);
		}
	}

	public void remove (VisualNode node) {
		node.setParent(null);
		children.remove(node);

		if (node instanceof VisualComponent)
			components.remove((VisualComponent)node);
		else if (node instanceof VisualGroup)
			groups.remove((VisualGroup)node);
		else if (node instanceof VisualConnection)
			connections.remove((VisualConnection)node);
		else
			misc.remove(node);
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
		else
			misc.add(node);

		node.setParent(this);
	}

	@Override
	public void clearColorisation() {
		setColorisation(null);
		for (VisualNode node : children)
			node.clearColorisation();
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D.Double rect = null;
		for(VisualComponent comp : components)
			rect = mergeRect(rect, comp);
		for(VisualGroup grp : groups)
			rect = mergeRect(rect, grp);

		return rect;
	}

	public final Collection<HierarchyNode> getChildren() {
		return new LinkedHashSet<HierarchyNode>(children);
	}

	public final Set<VisualComponent> getComponents() {
		return new LinkedHashSet<VisualComponent>(components);
	}

	public final Set<VisualConnection> getConnections() {
		return new LinkedHashSet<VisualConnection>(connections);
	}

	public VisualComponent hitComponent(Point2D pointInLocalSpace) {
		VisualComponent result = hitVisualNode(pointInLocalSpace, components);

		if(result!=null)
		{
			Point2D point2 = new Point2D.Double();
			result.getAncestorToParentTransform(this).transform(pointInLocalSpace, point2);
			result.getParentToLocalTransform().transform(point2, point2);
			return result.hitComponent(point2);
		}
		for (VisualGroup group : reverse(filterByBB(groups, pointInLocalSpace))) {
			Point2D pointInChildSpace = new Point2D.Double();
			group.parentToLocalTransform.transform(pointInLocalSpace, pointInChildSpace);
			result = group.hitComponent(pointInChildSpace);
			if(result!=null)
				return result;
		}
		return null;
	}

	public VisualConnection hitConnection(Point2D pointInLocalSpace) {
		VisualConnection result = hitVisualNode(pointInLocalSpace, connections);
		if(result!=null)
			return result;
		for (VisualGroup group : reverse(filterByBB(groups, pointInLocalSpace))) {
			Point2D pointInChildSpace = new Point2D.Double();
			group.parentToLocalTransform.transform(pointInLocalSpace, pointInChildSpace);
			result = group.hitConnection(pointInChildSpace);
			if(result!=null)
				return result;
		}
		return null;
	}

	public VisualNode hitNode(Point2D pointInLocalSpace) {
		VisualNode node = hitVisualNode(pointInLocalSpace, misc);
		if (node == null)
			node = hitVisualNode(pointInLocalSpace, components);
		if (node == null)
			node = hitVisualNode(pointInLocalSpace, groups);
		if (node == null)
			node = hitVisualNode(pointInLocalSpace, connections);
		return node;
	}



	public LinkedList<Touchable> hitObjects(Point2D p1, Point2D p2) {
		LinkedList<Touchable> hit = new LinkedList<Touchable>();

		Rectangle2D rect = new Rectangle2D.Double(
				Math.min(p1.getX(), p2.getX()),
				Math.min(p1.getY(), p2.getY()),
				Math.abs(p1.getX()-p2.getX()),
				Math.abs(p1.getY()-p2.getY()));

		for (Touchable n : childrenOfType(Touchable.class)) {
			if (p1.getX()<=p2.getX()) {
				if (TouchableHelper.insideRectangle(n, rect))
					hit.add(n);
			} else {
				if (TouchableHelper.touchesRectangle(n, rect))
					hit.add(n);
			}
		}
		return hit;
	}

	public LinkedList<Touchable> hitObjects(Rectangle2D rectInLocalSpace) {
		return hitObjects(
				new Point2D.Double(rectInLocalSpace.getMinX(), rectInLocalSpace.getMinY()),
				new Point2D.Double(rectInLocalSpace.getMaxX(), rectInLocalSpace.getMaxY()));
	}

	public Touchable hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return hitNode(pointInLocalSpace);
	}

	public void loadDeferredConnections(VisualModel model) throws VisualConnectionCreationException {
		for (VisualGroup g: groups)
			g.loadDeferredConnections(model);
		loadConnections(deferredGroupElement, model);
		deferredGroupElement = null;
	}

	@Override
	public void setColorisation(Color color) {
		super.setColorisation(color);
		for (VisualNode node : children)
			node.setColorisation(color);
	}

	public List<VisualNode> unGroup() {
		ArrayList<VisualNode> result = new ArrayList<VisualNode>(children.size());
		for (VisualConnection connection : connections.toArray(new VisualConnection[connections.size()])) {
			getParent().add(connection);
			result.add(connection);
		}

		try	{
			for (VisualTransformableNode group : groups.toArray(new VisualTransformableNode[groups.size()]))
			{
				getParent().add(group);
				result.add(group);
				group.applyTransform(localToParentTransform);
			}
			for (VisualTransformableNode component : components.toArray(new VisualTransformableNode[components.size()]))
			{
				getParent().add(component);
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

	@SuppressWarnings("unchecked")
	private <T> Collection<T> childrenOfType(Class<T> type)
	{
		ArrayList<T> result = new ArrayList<T>();

		for(HierarchyNode node : children)
		{
			if(type.isInstance(node))
				result.add((T)node);
		}
		return result;
	}

	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		for (DependentNode n: childrenOfType(DependentNode.class))
			ret.addAll(n.getMathReferences());
		return ret;
	}

	public VisualNodeSerialiser getSerialiser() {
		return new VisualNodeSerialiser()
		{
			public void serialise(HierarchyNode node, Element element) {
				((VisualGroup)node).serialiseToXML(element);
			}
		};
	}
}