package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.util.XmlUtil;


public abstract class VisualTransformableNode extends VisualNode {
	private Point2D _tmpPoint = new Point2D.Double();

	protected AffineTransform localToParentTransform = new AffineTransform();
	protected AffineTransform parentToLocalTransform = new AffineTransform();

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration("X", "getX", "setX", double.class));
		addPropertyDeclaration(new PropertyDeclaration("Y", "getY", "setY", double.class));
	}

	private void addXMLSerialiser() {
		addXMLSerialiser(new XMLSerialiser(){
			public String getTagName() {
				return VisualTransformableNode.class.getSimpleName();
			}
			public void serialise(Element element) {
				XmlUtil.writeDoubleAttr(element, "X", getX());
				XmlUtil.writeDoubleAttr(element, "Y", getY());
			}
		});
	}

	public VisualTransformableNode() {
		super();
		addPropertyDeclarations();
		addXMLSerialiser();
	}

	public VisualTransformableNode (Element visualNodeElement) {
		super();
		addPropertyDeclarations();
		addXMLSerialiser();

		Element vnodeElement = XmlUtil.getChildElement(VisualTransformableNode.class.getSimpleName(), visualNodeElement);
		setX (XmlUtil.readDoubleAttr(vnodeElement, "X", 0));
		setY (XmlUtil.readDoubleAttr(vnodeElement, "Y", 0));
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

	public void setPosition(Point2D pos) {
		localToParentTransform.translate(pos.getX()-localToParentTransform.getTranslateX(), pos.getY() - localToParentTransform.getTranslateY());
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
		firePropertyChanged("transform");
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
		localToParentTransform.preConcatenate(transform);
		transformChanged();
	}
}