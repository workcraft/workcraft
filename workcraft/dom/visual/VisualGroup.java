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
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.util.Hierarchy;


public class VisualGroup extends VisualTransformableNode implements Drawable, Container {
	public static final int HIT_COMPONENT = 1;
	public static final int HIT_CONNECTION = 2;
	public static final int HIT_GROUP = 3;

	protected Set<HierarchyNode> children = new LinkedHashSet<HierarchyNode>();

	//private Element deferredGroupElement = null;
	//private String label = "";


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
		for (Colorisable node : NodeHelper.getChildrenOfType(this, Colorisable.class))
			node.clearColorisation();
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return BoundingBoxHelper.mergeBoundingBoxes(NodeHelper.getChildrenOfType(this, Touchable.class));
	}

	public final Collection<HierarchyNode> getChildren() {
		return new LinkedHashSet<HierarchyNode>(children);
	}

	public final Collection<VisualComponent> getComponents() {
		return NodeHelper.getChildrenOfType(this, VisualComponent.class);
	}

	public final Collection<VisualConnection> getConnections() {
		return NodeHelper.getChildrenOfType(this, VisualConnection.class);
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

		for (Touchable n : NodeHelper.getChildrenOfType(this, Touchable.class)) {
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
		for (Colorisable node : NodeHelper.getChildrenOfType(this, Colorisable.class))
			node.setColorisation(color);
	}

	public List<HierarchyNode> unGroup() {
		ArrayList<HierarchyNode> result = new ArrayList<HierarchyNode>(children.size());

		Container parent = Hierarchy.getNearestAncestor(getParent(), Container.class);

		for (HierarchyNode node : children) {
			node.setParent(null);
			parent.add(node);
			result.add(node);
		}

		TransformHelper.applyTransformToChildNodes(this, localToParentTransform);

		children.clear();

		return result;
	}

	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		for (DependentNode n: NodeHelper.getChildrenOfType(this, DependentNode.class))
			ret.addAll(n.getMathReferences());
		return ret;
	}

	public VisualNodeSerialiser getSerialiser() {
		return new VisualNodeSerialiser()
		{
			public void serialise(HierarchyNode node, Element element,
					ReferenceProducer referenceResolver) {
				((VisualGroup)node).serialise(element, referenceResolver);
			}
		};
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return false;
	}
}