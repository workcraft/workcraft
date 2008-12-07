package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.util.XmlUtil;


public abstract class VisualNode implements PropertyEditable, Selectable {

	protected AffineTransform transform;
	protected LinkedList<PropertyDeclaration> propertyDeclarations;
	protected int qwe = 0;

	public VisualNode() {
		this.transform = new AffineTransform();
		this.transform.setToIdentity();

		this.propertyDeclarations = new LinkedList<PropertyDeclaration>();

		this.propertyDeclarations.add(new PropertyDeclaration("X", "getX", "setX", Double.class));
		this.propertyDeclarations.add(new PropertyDeclaration("Y", "getY", "setY", Double.class));
		this.propertyDeclarations.add(new PropertyDeclaration("qwe", "getQwe", "setQwe", Integer.class));
	}

	public VisualNode (Element xmlElement) {
		NodeList nodes = xmlElement.getElementsByTagName("node");
		Element vnodeElement = (Element)nodes.item(0);
		setX (XmlUtil.readDoubleAttr(vnodeElement, "x", 0));
		setY (XmlUtil.readDoubleAttr(vnodeElement, "y", 0));
	}

	public int getQwe() {
		return this.qwe;
	}

	public void setQwe(int qwe) {
		this.qwe = qwe;

	}

	public double getX() {
		return this.transform.getTranslateX();
	}

	public double getY() {
		return this.transform.getTranslateY();
	}

	public void setX(double x) {
		this.transform.translate(x-this.transform.getTranslateX(), 0);
	}

	public void setY(double y) {
		this.transform.translate(0, y - this.transform.getTranslateY());
	}

	public abstract void draw (Graphics2D g);

	public void toXML(Element xmlElement) {
		Element vnodeElement = xmlElement.getOwnerDocument().createElement("node");
		XmlUtil.writeDoubleAttr(vnodeElement, "x", getX());
		XmlUtil.writeDoubleAttr(vnodeElement, "y", getY());
		xmlElement.appendChild(vnodeElement);
	}


	public List<PropertyDeclaration> getPropertyDeclarations() {
		return this.propertyDeclarations;
	}
}