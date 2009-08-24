package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.util.XmlUtil;


public class VisualGroup extends VisualTransformableNode implements Drawable, Container {
	public static final int HIT_COMPONENT = 1;
	public static final int HIT_CONNECTION = 2;
	public static final int HIT_GROUP = 3;

	protected Set<FreeNode> children = new LinkedHashSet<FreeNode>();

	private Element deferredGroupElement = null;
	private String label = "";

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
		this.addPropertyChangeListener(new PropertyChangeListener(){
			public void onPropertyChanged(String propertyName, Object sender) {
				if(propertyName == "transform")
					for(HierarchyNode node : children)
					{
						if(node instanceof Movable && node instanceof PropertyEditable)
							((PropertyEditable)node).firePropertyChanged("transform");
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
	public void draw(Graphics2D g) {
		Rectangle2D bb = getBoundingBoxInLocalSpace();

		if (bb != null && getParent() != null) {
			bb.setRect(bb.getX() - 0.1, bb.getY() - 0.1, bb.getWidth() + 0.2, bb.getHeight() + 0.2);
			g.setColor(Coloriser.colorise(Color.GRAY, getColorisation()));
			g.setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{0.2f, 0.2f}, 0.0f));
			g.draw(bb);
		}
	}

	public void remove (FreeNode node) {
		node.setParent(null);
		children.remove(node);
	}

	public void add (FreeNode node) {
		if (node.getParent() == this)
			return;

		if (node.getParent() != null)
			throw new RuntimeException("Cannot attach a someone else's node. Please detach from an old parent first.");

		children.add(node);

		node.setParent(this);
	}

	@Override
	public void clearColorisation() {
		setColorisation(null);
		for (Colorisable node : getChildrenOfType(Colorisable.class))
			node.clearColorisation();
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D.Double rect = null;
		for(VisualComponent comp : getChildrenOfType(VisualComponent.class))
			rect = mergeRect(rect, comp);
		for(VisualGroup grp : getChildrenOfType(VisualGroup.class))
			rect = mergeRect(rect, grp);

		return rect;
	}

	public final Collection<HierarchyNode> getChildren() {
		return new LinkedHashSet<HierarchyNode>(children);
	}

	public final Collection<VisualComponent> getComponents() {
		return getChildrenOfType(VisualComponent.class);
	}

	public final Collection<VisualConnection> getConnections() {
		return getChildrenOfType(VisualConnection.class);
	}

	public LinkedList<Touchable> hitObjects(Point2D p1 , Point2D p2) {
		Point2D p1local = new Point2D.Double();
		Point2D p2local = new Point2D.Double();
		this.getParentToLocalTransform().transform(p1, p1local);
		this.getParentToLocalTransform().transform(p2, p2local);

		LinkedList<Touchable> hit = new LinkedList<Touchable>();

		Rectangle2D rect = new Rectangle2D.Double(
				Math.min(p1local.getX(), p2local.getX()),
				Math.min(p1local.getY(), p2local.getY()),
				Math.abs(p1local.getX()-p2local.getX()),
				Math.abs(p1local.getY()-p2local.getY()));

		for (Touchable n : getChildrenOfType(Touchable.class)) {
			if (p1local.getX()<=p2local.getX()) {
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

	public void loadDeferredConnections(VisualModel model) throws VisualConnectionCreationException {
		for (VisualGroup g: getChildrenOfType(VisualGroup.class))
			g.loadDeferredConnections(model);
		loadConnections(deferredGroupElement, model);
		deferredGroupElement = null;
	}

	@Override
	public void setColorisation(Color color) {
		super.setColorisation(color);
		for (Colorisable node : getChildrenOfType(Colorisable.class))
			node.setColorisation(color);
	}

	public List<HierarchyNode> unGroup() {
		ArrayList<HierarchyNode> result = new ArrayList<HierarchyNode>(children.size());

		Container parent = HierarchyHelper.getNearestAncestor(getParent(), Container.class);

		for (FreeNode node : children) {
			node.setParent(null);
			parent.add(node);
			result.add(node);
		}

		TransformHelper.applyTransformToChildNodes(this, localToParentTransform);

		children.clear();

		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> getChildrenOfType(Class<T> type)
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
		for (DependentNode n: getChildrenOfType(DependentNode.class))
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

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return false;
	}
}