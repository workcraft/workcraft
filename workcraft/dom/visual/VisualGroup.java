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
import org.workcraft.dom.Container;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.VisualNodeSerialiser;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyEditable;


public class VisualGroup extends VisualTransformableNode implements Drawable, Container {
	public static final int HIT_COMPONENT = 1;
	public static final int HIT_CONNECTION = 2;
	public static final int HIT_GROUP = 3;

	protected Set<HierarchyNode> children = new LinkedHashSet<HierarchyNode>();

	//private Element deferredGroupElement = null;
	//private String label = "";

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
		addPropertyChangeListener();
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

	public void draw(Graphics2D g) {
		Rectangle2D bb = getBoundingBoxInLocalSpace();

		if (bb != null && getParent() != null) {
			bb.setRect(bb.getX() - 0.1, bb.getY() - 0.1, bb.getWidth() + 0.2, bb.getHeight() + 0.2);
			g.setColor(Coloriser.colorise(Color.GRAY, getColorisation()));
			g.setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{0.2f, 0.2f}, 0.0f));
			g.draw(bb);
		}
	}

	public void remove (HierarchyNode node) {
		node.setParent(null);
		children.remove(node);
	}

	public void add (HierarchyNode node) {
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

	public void setColorisation(Color color) {
		super.setColorisation(color);
		for (Colorisable node : getChildrenOfType(Colorisable.class))
			node.setColorisation(color);
	}

	public List<HierarchyNode> unGroup() {
		ArrayList<HierarchyNode> result = new ArrayList<HierarchyNode>(children.size());

		Container parent = HierarchyHelper.getNearestAncestor(getParent(), Container.class);

		for (HierarchyNode node : children) {
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
			public void serialise(HierarchyNode node, Element element,
					ExternalReferenceResolver referenceResolver) {
				((VisualGroup)node).serialise(element, referenceResolver);
			}
		};
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return false;
	}
}