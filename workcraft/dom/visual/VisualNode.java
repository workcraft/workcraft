package org.workcraft.dom.visual;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyEditable;


public abstract class VisualNode implements PropertyEditable {
	protected LinkedList<PropertyDeclaration> propertyDeclarations;
	protected LinkedList<PropertyChangeListener> propertyChangeListeners;
	protected Color colorisation;
	protected VisualComponentGroup parent;

	public VisualNode(VisualComponentGroup parent) {
		this.parent = parent;
		colorisation = null;

		propertyChangeListeners = new LinkedList<PropertyChangeListener>();
		propertyDeclarations = new LinkedList<PropertyDeclaration>();
	}

	public VisualNode (Element xmlElement, VisualComponentGroup parent) {
		this(parent);
		// NodeList nodes = xmlElement.getElementsByTagName("node");
		// Element vnodeElement = (Element)nodes.item(0);
	}

	public void toXML(Element xmlElement) {
		Element vnodeElement = xmlElement.getOwnerDocument().createElement("node");
		xmlElement.appendChild(vnodeElement);
	}

	public VisualComponentGroup getParent() {
		return parent;
	}

	public void setParent(VisualComponentGroup parent) {
		this.parent = parent;
	}

	public abstract void draw (Graphics2D g);

	public abstract int hitTestInParentSpace(Point2D pointInParentSpace);

	public int hitTestInAncestorSpace(Point2D pointInUserSpace, VisualComponentGroup ancestor) throws NotAnAncestorException {

		if (ancestor != parent) {
			Point2D pt = new Point2D.Double();
			pt.setLocation(pointInUserSpace);
			AffineTransform t = getParentToAncestorTransform(ancestor);
			t.transform(pt,pt);
			return hitTestInParentSpace(pt);
		} else
			return hitTestInParentSpace(pointInUserSpace);
	}

	private AffineTransform optimisticInverse(AffineTransform transform)
	{
		try
		{
			return transform.createInverse();
		}
		catch(NoninvertibleTransformException ex)
		{
			throw new RuntimeException("Matrix inverse failed!");
		}
	}

	public final AffineTransform getAncestorToParentTransform(VisualComponentGroup ancestor) throws NotAnAncestorException {
		return optimisticInverse(getParentToAncestorTransform(ancestor));
	}

	public final AffineTransform getParentToAncestorTransform(VisualComponentGroup ancestor) throws NotAnAncestorException{
		AffineTransform t = new AffineTransform();

		VisualComponentGroup next = parent;
		while (ancestor != next) {
			if (next == null)
				throw new NotAnAncestorException();
			t.concatenate(next.getLocalToParentTransform());
			next = next.parent;
		}

		return t;
	}

	public abstract Rectangle2D getBoundingBoxInParentSpace();

	public final Rectangle2D getBoundingBoxInAncestorSpace(VisualComponentGroup ancestor) throws NotAnAncestorException {
		Rectangle2D parentBB = getBoundingBoxInParentSpace();

		Point2D p0 = new Point2D.Double(parentBB.getMinX(), parentBB.getMinY());
		Point2D p1 = new Point2D.Double(parentBB.getMaxX(), parentBB.getMaxY());

		AffineTransform t = getParentToAncestorTransform(ancestor);
		t.transform(p0, p0);
		t.transform(p1, p1);

		return new Rectangle2D.Double (
				p0.getX(), p0.getY(),
				p1.getX()-p0.getX(),p1.getY() - p0.getY()
		);
	}

	public List<PropertyDeclaration> getPropertyDeclarations() {
		return propertyDeclarations;
	}

	public void addListener(PropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}

	public void removeListener(PropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}

	public void firePropertyChanged(String propertyName) {
		for (PropertyChangeListener l : propertyChangeListeners)
			l.propertyChanged(propertyName, this);
	}

	public void setColorisation (Color color) {
		colorisation = color;
	}

	public Color getColorisation (Color color) {
		return colorisation;
	}

	public void clearColorisation() {
		setColorisation(null);
	}

	public final boolean isDescendantOf(VisualComponentGroup group) {
		VisualNode node = this;
		while(node != group)
		{
			if(node == null)
				return false;
			node = node.parent;
		}
		return true;
	}
}