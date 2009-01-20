package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.util.XmlUtil;


public abstract class VisualTransformableNode extends VisualNode {
	private Point2D _tmpPoint = new Point2D.Double();

	protected AffineTransform localToParentTransform = new AffineTransform();
	protected AffineTransform parentToLocalTransform = new AffineTransform();

	private void addPropertyDeclarations() {
		propertyDeclarations.add(new PropertyDeclaration("X", "getX", "setX", double.class));
		propertyDeclarations.add(new PropertyDeclaration("Y", "getY", "setY", double.class));
	}

	public VisualTransformableNode(VisualComponentGroup parent) {
		super(parent);
		addPropertyDeclarations();
	}

	public VisualTransformableNode (Element xmlElement, VisualComponentGroup parent) {
		super (xmlElement, parent);
		addPropertyDeclarations();
		NodeList nodes = xmlElement.getElementsByTagName("transform");
		Element vnodeElement = (Element)nodes.item(0);
		setX (XmlUtil.readDoubleAttr(vnodeElement, "X", 0));
		setY (XmlUtil.readDoubleAttr(vnodeElement, "Y", 0));
	}



	public void toXML(Element xmlElement) {
		super.toXML(xmlElement);
		Element vnodeElement = xmlElement.getOwnerDocument().createElement("transform");
		XmlUtil.writeDoubleAttr(vnodeElement, "X", getX());
		XmlUtil.writeDoubleAttr(vnodeElement, "Y", getY());
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

	public Point2D getPosition() {
		return new Point2D.Double(getX(), getY());
	}

	protected void transformChanged() {
		try {
			parentToLocalTransform = localToParentTransform.createInverse();
		} catch (NoninvertibleTransformException e) {
			System.err.println(e.getMessage());
		}
	}

	public abstract int hitTestInLocalSpace(Point2D pointInLocalSpace);

	public final int hitTestInParentSpace(Point2D pointInParentSpace) {
		parentToLocalTransform.transform(pointInParentSpace, _tmpPoint);
		return hitTestInLocalSpace(_tmpPoint);
	}

	@Override
	public final void draw(java.awt.Graphics2D g) {
		g.transform(localToParentTransform);
		drawInLocalSpace(g);
	};

	protected void drawInLocalSpace(java.awt.Graphics2D g) {
	}

	public abstract Rectangle2D getBoundingBoxInLocalSpace();

    public final Rectangle2D getBoundingBoxInParentSpace() {
    	Rectangle2D parentBB = getBoundingBoxInLocalSpace();
    	if(parentBB == null)
    		return null;

		Point2D p0 = new Point2D.Double(parentBB.getMinX(), parentBB.getMinY());
		Point2D p1 = new Point2D.Double(parentBB.getMaxX(), parentBB.getMaxY());

		AffineTransform t = getLocalToParentTransform();
		t.transform(p0, p0);
		t.transform(p1, p1);

		return new Rectangle2D.Double (
				p0.getX(), p0.getY(),
				p1.getX()-p0.getX(),p1.getY() - p0.getY()
		);
     }


	public AffineTransform getLocalToParentTransform() {
		return localToParentTransform;
	}

	public AffineTransform getParentToLocalTransform() {
		return parentToLocalTransform;
	}

	public void applyTransform(AffineTransform transform) throws NoninvertibleTransformException
	{
		parentToLocalTransform.concatenate(transform.createInverse());
		localToParentTransform.preConcatenate(transform);
	}
}