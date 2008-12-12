package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.util.XmlUtil;


public abstract class VisualNode implements PropertyEditable, Selectable {
	private double[] _tmp;

	protected AffineTransform transform;
	protected LinkedList<PropertyDeclaration> propertyDeclarations;

	VisualComponentGroup parent;

	public VisualNode(VisualComponentGroup parent) {
		transform = new AffineTransform();
		transform.setToIdentity();

		this.parent = parent;

		propertyDeclarations = new LinkedList<PropertyDeclaration>();

		propertyDeclarations.add(new PropertyDeclaration("X", "getX", "setX", Double.class));
		propertyDeclarations.add(new PropertyDeclaration("Y", "getY", "setY", Double.class));

		_tmp = new double[8];

	}

	public VisualNode (Element xmlElement, VisualComponentGroup parent) {
		this(parent);
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

	public Point2D getPositionInParentSpace() {
		Point2D src = new Point2D.Double(0,0);
		Point2D dst = new Point2D.Double();
		transform.transform(src, dst);
		return dst;
	}

	public Point2D getPositionInUserSpace() {
		VisualComponentGroup p = parent;
		Point2D src = getPositionInParentSpace();
		Point2D dst = new Point2D.Double();

		while (p!=null) {
			p.getTransform().transform(src, dst);
			src.setLocation(dst);
			p = p.getParent();
		}

		return dst;
	}

	public VisualComponentGroup getParent() {
		return parent;
	}

	public abstract void draw (Graphics2D g);

	public void toXML(Element xmlElement) {
		Element vnodeElement = xmlElement.getOwnerDocument().createElement("node");
		XmlUtil.writeDoubleAttr(vnodeElement, "x", getX());
		XmlUtil.writeDoubleAttr(vnodeElement, "y", getY());
		xmlElement.appendChild(vnodeElement);
	}

	public Rectangle2D getBoundingBoxInParentSpace() {
		Rectangle2D localBB = getBoundingBoxInLocalSpace();

		_tmp[0] = localBB.getMinX();
		_tmp[1] = localBB.getMinY();
		_tmp[2] = localBB.getMaxX();
		_tmp[3] = localBB.getMaxY();

		transform.transform(_tmp, 0, _tmp, 0, 2);

		return new Rectangle2D.Double (
				_tmp[0], _tmp[1],
				_tmp[2]-_tmp[0], _tmp[3]-_tmp[1]
		);
	}

	public boolean hitTestInParentSpace(Point2D pointInParentSpace) {
		return getBoundingBoxInParentSpace().contains(pointInParentSpace);
	}

	public Rectangle2D getBoundingBoxInUserSpace() {
		Rectangle2D bb = getBoundingBoxInParentSpace();
		VisualComponentGroup p = parent;

		while (p != null) {
			_tmp[0] = bb.getMinX();
			_tmp[1] = bb.getMinY();
			_tmp[2] = bb.getMaxX();
			_tmp[3] = bb.getMaxY();

			p.getTransform().transform(_tmp, 0, _tmp, 0, 2);

			bb.setRect(_tmp[0], _tmp[1], _tmp[2]-_tmp[0], _tmp[3]-_tmp[1]);
			p = p.getParent();
		}

		return bb;
	}

	public boolean hitTestInUserSpace(Point2D pointInUserSpace) {
		return false;
	}

	public List<PropertyDeclaration> getPropertyDeclarations() {
		return propertyDeclarations;
	}

	public AffineTransform getTransform() {
		return transform;
	}
}