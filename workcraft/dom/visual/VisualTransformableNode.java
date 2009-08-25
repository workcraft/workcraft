package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.w3c.dom.Element;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.framework.exceptions.ImportException;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.util.Geometry;
import org.workcraft.util.XmlUtil;


public abstract class VisualTransformableNode extends VisualNode implements Movable {
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

			public void deserialise(Element element,
					ReferenceResolver refResolver) throws ImportException {
				setX (XmlUtil.readDoubleAttr(element, "X", 0));
				setY (XmlUtil.readDoubleAttr(element, "Y", 0));
			}

			public void serialise(Element element,
					ExternalReferenceResolver refResolver) {
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

		VisualTransformableNodeDeserialiser.initTransformableNode(visualNodeElement, this);
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
		parentToLocalTransform = Geometry.optimisticInverse(localToParentTransform);
		firePropertyChanged("transform");
	}

	public abstract boolean hitTestInLocalSpace(Point2D pointInLocalSpace);

	public boolean hitTest(Point2D point) {
		parentToLocalTransform.transform(point, _tmpPoint);
		return hitTestInLocalSpace(_tmpPoint);
	}

	public abstract Rectangle2D getBoundingBoxInLocalSpace();

    public final Rectangle2D getBoundingBox() {
    	Rectangle2D parentBB = getBoundingBoxInLocalSpace();
    	if(parentBB == null)
    		return null;

		Point2D p0 = new Point2D.Double(parentBB.getMinX(), parentBB.getMinY());
		Point2D p1 = new Point2D.Double(parentBB.getMaxX(), parentBB.getMaxY());

		AffineTransform t = getLocalToParentTransform();
		t.transform(p0, p0);
		t.transform(p1, p1);

		Rectangle2D.Double result = new Rectangle2D.Double(p0.getX(), p0.getY(), 0, 0);
		result.add(p1);

		return result;
    }

	public AffineTransform getLocalToParentTransform() {
		return localToParentTransform;
	}

	public AffineTransform getParentToLocalTransform() {
		return parentToLocalTransform;
	}

	public void applyTransform(AffineTransform transform)
	{
		localToParentTransform.preConcatenate(transform);
		transformChanged();
	}

	public double getRotation() {
		return 0;
	}

	public double getScaleX() {
		return 0;
	}

	public double getScaleY() {
		return 0;
	}

	public AffineTransform getTransform() {
		return getLocalToParentTransform();
	}

	public void setRotation(double rotation) {

	}

	public void setScaleX(double scaleX) {

	}

	public void setScaleY(double scaleY) {

	}

	public void setTransform(AffineTransform transform) {
		localToParentTransform.setTransform(transform);
		transformChanged();
	}
}