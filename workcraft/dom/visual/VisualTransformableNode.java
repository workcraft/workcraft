package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.util.XmlUtil;


public abstract class VisualTransformableNode extends VisualNode {
	private double[] _tmp = new double[8];
	private Point2D _tmpPoint = new Point2D.Double();

	protected AffineTransform localToParentTransform = new AffineTransform();
	protected AffineTransform parentToLocalTransform = new AffineTransform();

	public VisualTransformableNode(VisualComponentGroup parent) {
		super(parent);
	}

	public VisualTransformableNode (Element xmlElement, VisualComponentGroup parent) {
		super (xmlElement, parent);
		NodeList nodes = xmlElement.getElementsByTagName("transform");
		Element vnodeElement = (Element)nodes.item(0);
		setX (XmlUtil.readDoubleAttr(vnodeElement, "x", 0));
		setY (XmlUtil.readDoubleAttr(vnodeElement, "y", 0));
	}

	public void toXML(Element xmlElement) {
		super.toXML(xmlElement);
		Element vnodeElement = xmlElement.getOwnerDocument().createElement("transform");
		XmlUtil.writeDoubleAttr(vnodeElement, "x", getX());
		XmlUtil.writeDoubleAttr(vnodeElement, "y", getY());
		xmlElement.appendChild(vnodeElement);
	}

	public double getX() {
		return localToParentTransform.getTranslateX();
	}

	public double getY() {
		return localToParentTransform.getTranslateY();
	}

	public void setX(double x) {
		localToParentTransform.translate(x-localToParentTransform.getTranslateX(), 0);
		transformChanged();
	}

	public void setY(double y) {
		localToParentTransform.translate(0, y - localToParentTransform.getTranslateY());
		transformChanged();
	}

	public Point2D getPositionInParentSpace() {
		Point2D src = new Point2D.Double(0,0);
		Point2D dst = new Point2D.Double();
		localToParentTransform.transform(src, dst);
		return dst;
	}

	public Point2D getPositionInUserSpace() {
		VisualComponentGroup p = parent;
		Point2D src = getPositionInParentSpace();
		Point2D dst = new Point2D.Double();

		while (p!=null) {
			p.getLocalToParentTransform().transform(src, dst);
			src.setLocation(dst);
			p = p.getParent();
		}

		return dst;
	}

	protected void transformChanged() {
		try {
			parentToLocalTransform = localToParentTransform.createInverse();
		} catch (NoninvertibleTransformException e) {
			System.err.println(e.getMessage());
		}
	}

	public int hitTestInParentSpace(Point2D pointInParentSpace) {
		parentToLocalTransform.transform(pointInParentSpace, _tmpPoint);
		return hitTestInLocalSpace(_tmpPoint);
	}

	public int hitTestInUserSpace(Point2D pointInUserSpace) {
		VisualComponentGroup p = parent;

		LinkedList<AffineTransform> transformStack = new LinkedList<AffineTransform>();

		// walk up the hierarchy to the root node
		while (p.getParent()!=null) {
			transformStack.push(p.getParentToLocalTransform());
			p = p.getParent();
		}

		_tmpPoint.setLocation(pointInUserSpace);

		// apply all transforms from root to this node's parent
		for (AffineTransform t: transformStack)
			t.transform(_tmpPoint, _tmpPoint);

		// now the point is in parent space
		return hitTestInParentSpace(_tmpPoint);
	}

	public Rectangle2D getBoundingBoxInParentSpace() {
		Rectangle2D localBB = getBoundingBoxInLocalSpace();

		_tmp[0] = localBB.getMinX();
		_tmp[1] = localBB.getMinY();
		_tmp[2] = localBB.getMaxX();
		_tmp[3] = localBB.getMaxY();

		localToParentTransform.transform(_tmp, 0, _tmp, 0, 2);

		return new Rectangle2D.Double (
				_tmp[0], _tmp[1],
				_tmp[2]-_tmp[0], _tmp[3]-_tmp[1]
		);
	}

	public Rectangle2D getBoundingBoxInUserSpace() {
		Rectangle2D bb = getBoundingBoxInParentSpace();
		VisualComponentGroup p = parent;

		while (p != null) {
			_tmp[0] = bb.getMinX();
			_tmp[1] = bb.getMinY();
			_tmp[2] = bb.getMaxX();
			_tmp[3] = bb.getMaxY();

			p.getLocalToParentTransform().transform(_tmp, 0, _tmp, 0, 2);

			bb.setRect(_tmp[0], _tmp[1], _tmp[2]-_tmp[0], _tmp[3]-_tmp[1]);
			p = p.getParent();
		}

		return bb;
	}

	public AffineTransform getLocalToParentTransform() {
		return localToParentTransform;
	}

	public AffineTransform getParentToLocalTransform() {
		return parentToLocalTransform;
	}
}