package org.workcraft.dom.visual;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyEditableListener;


public abstract class VisualNode implements PropertyEditable {
	protected LinkedList<PropertyDeclaration> propertyDeclarations;
	protected LinkedList<PropertyEditableListener> propertyEditableListeners;
	protected Color colorisation;
	protected VisualComponentGroup parent;

	public VisualNode(VisualComponentGroup parent) {
		this.parent = parent;
		colorisation = null;

		propertyEditableListeners = new LinkedList<PropertyEditableListener>();
		propertyDeclarations = new LinkedList<PropertyDeclaration>();

		propertyDeclarations.add(new PropertyDeclaration("X", "getX", "setX", double.class));
		propertyDeclarations.add(new PropertyDeclaration("Y", "getY", "setY", double.class));
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

	public abstract int hitTestInLocalSpace(Point2D pointInLocalSpace);
	public abstract int hitTestInParentSpace(Point2D pointInParentSpace);
	public abstract int hitTestInUserSpace(Point2D pointInUserSpace);

	public abstract Rectangle2D getBoundingBoxInLocalSpace();
	public abstract Rectangle2D getBoundingBoxInParentSpace();
	public abstract Rectangle2D getBoundingBoxInUserSpace();

	public List<PropertyDeclaration> getPropertyDeclarations() {
		return propertyDeclarations;
	}

	public void addListener(PropertyEditableListener listener) {
		propertyEditableListeners.add(listener);
	}

	public void removeListener(PropertyEditableListener listener) {
		propertyEditableListeners.remove(listener);
	}

	public void firePropertyChanged(String propertyName) {
		for (PropertyEditableListener l : propertyEditableListeners)
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
}