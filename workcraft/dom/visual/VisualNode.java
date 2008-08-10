package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.util.XmlUtil;

public abstract class VisualNode {
	protected AffineTransform transform;

	public VisualNode() {
		transform = new AffineTransform();
		transform.setToIdentity();
	}

	public VisualNode (Element xmlElement) {
		NodeList nodes = xmlElement.getElementsByTagName("node");
		Element vnodeElement = (Element)nodes.item(0);
		setX (XmlUtil.readDoubleAttr(vnodeElement, "x", 0));
		setY (XmlUtil.readDoubleAttr(vnodeElement, "y", 0));
	}

	public double getX() {
		return transform.getTranslateX();
	}

	public double getY() {
		return transform.getTranslateY();
	}

	public void setX(double x) {
		transform.translate(x-transform.getTranslateX(), 0);
	}

	public void setY(double y) {
		transform.translate(0, y - transform.getTranslateY());
	}

	public abstract void draw (Graphics2D g);

	public void toXML(Element xmlElement) {
		Element vnodeElement = xmlElement.getOwnerDocument().createElement("node");
		XmlUtil.writeDoubleAttr(vnodeElement, "x", getX());
		XmlUtil.writeDoubleAttr(vnodeElement, "y", getY());
		xmlElement.appendChild(vnodeElement);
	}
}