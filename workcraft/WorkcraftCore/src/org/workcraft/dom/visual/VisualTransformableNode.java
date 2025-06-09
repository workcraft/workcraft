package org.workcraft.dom.visual;

import org.w3c.dom.Element;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.Geometry;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public abstract class VisualTransformableNode extends VisualNode implements Movable, Rotatable, Flippable {

    public static final String PROPERTY_X = "X";
    public static final String PROPERTY_Y = "Y";

    protected AffineTransform localToParentTransform = new AffineTransform();
    protected AffineTransform parentToLocalTransform = new AffineTransform();

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Double.class, PROPERTY_X,
                this::setRootSpaceX, this::getRootSpaceX).setCombinable());

        addPropertyDeclaration(new PropertyDeclaration<>(Double.class, PROPERTY_Y,
                this::setRootSpaceY, this::getRootSpaceY).setCombinable());
    }

    public VisualTransformableNode() {
        super();
        addPropertyDeclarations();
    }

    public VisualTransformableNode(Element visualNodeElement) {
        super();
        addPropertyDeclarations();
        VisualTransformableNodeDeserialiser.initTransformableNode(visualNodeElement, this);
    }

    @NoAutoSerialisation
    public final double getX() {
        return getLocalToParentTransform().getTranslateX();
    }

    @NoAutoSerialisation
    public final void setX(double value) {
        transformChanging();
        double dx = value - getLocalToParentTransform().getTranslateX();
        localToParentTransform.translate(dx, 0.0);
        transformChanged();
    }

    @NoAutoSerialisation
    public final double getRootSpaceX() {
        double result = 0.0;
        Node node = this;
        while (node != null) {
            if (node instanceof VisualTransformableNode) {
                result += ((VisualTransformableNode) node).getX();
            }
            node = node.getParent();
        }
        return result;
    }

    @NoAutoSerialisation
    public final void setRootSpaceX(double value) {
        Node node = getParent();
        while (node != null) {
            if (node instanceof VisualTransformableNode) {
                value -= ((VisualTransformableNode) node).getX();
            }
            node = node.getParent();
        }
        setX(value);
    }

    @NoAutoSerialisation
    public final double getY() {
        return getLocalToParentTransform().getTranslateY();
    }

    @NoAutoSerialisation
    public final void setY(double value) {
        transformChanging();
        double dy = value - getLocalToParentTransform().getTranslateY();
        localToParentTransform.translate(0.0, dy);
        transformChanged();
    }

    @NoAutoSerialisation
    public final double getRootSpaceY() {
        double result = 0.0;
        Node node = this;
        while (node != null) {
            if (node instanceof VisualTransformableNode) {
                result += ((VisualTransformableNode) node).getY();
            }
            node = node.getParent();
        }
        return result;
    }

    @NoAutoSerialisation
    public final void setRootSpaceY(double value) {
        Node node = getParent();
        while (node != null) {
            if (node instanceof VisualTransformableNode) {
                value -= ((VisualTransformableNode) node).getY();
            }
            node = node.getParent();
        }
        setY(value);
    }

    @NoAutoSerialisation
    public final void setRootSpacePosition(Point2D pos) {
        Node node = getParent();
        double x = pos.getX();
        double y = pos.getY();
        while (node != null) {
            if (node instanceof VisualTransformableNode transferableNode) {
                x -= transferableNode.getX();
                y -= transferableNode.getY();
            }
            node = node.getParent();
        }
        setPosition(new Point2D.Double(x, y));
    }

    @NoAutoSerialisation
    public final Point2D getRootSpacePosition() {
        return new Point2D.Double(getRootSpaceX(), getRootSpaceY());
    }

    @NoAutoSerialisation
    public final void setPosition(Point2D pos) {
        transformChanging();
        double dx = pos.getX() - getLocalToParentTransform().getTranslateX();
        double dy = pos.getY() - getLocalToParentTransform().getTranslateY();
        localToParentTransform.translate(dx, dy);
        transformChanged();
    }

    @NoAutoSerialisation
    public final Point2D getPosition() {
        return new Point2D.Double(getX(), getY());
    }

    @NoAutoSerialisation
    public final Point2D getRootSpaceTranslation() {
        double dx = 0.0;
        double dy = 0.0;
        Node node = this;
        do {
            if (node instanceof VisualTransformableNode) {
                dx += ((VisualTransformableNode) node).getX();
                dy += ((VisualTransformableNode) node).getY();
            }
            node = node.getParent();
        } while (node != null);
        return new Point2D.Double(dx, dy);
    }

    protected void transformChanging() {
        sendNotification(new TransformChangingEvent(this));
    }

    protected void transformChanged() {
        parentToLocalTransform = Geometry.optimisticInverse(getLocalToParentTransform());
        sendNotification(new TransformChangedEvent(this));
    }

    public abstract boolean hitTestInLocalSpace(Point2D pointInLocalSpace);

    @Override
    public boolean hitTest(Point2D point) {
        Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
        return hitTestInLocalSpace(pointInLocalSpace);
    }

    public abstract Rectangle2D getBoundingBoxInLocalSpace();

    @Override
    public final Rectangle2D getBoundingBox() {
        return transformToParentSpace(getBoundingBoxInLocalSpace());
    }

    @Override
    public final Point2D getCenter() {
        return getLocalToParentTransform().transform(new Point2D.Double(0.0, 0.0), null);
    }

    protected Rectangle2D transformToParentSpace(Rectangle2D rect) {
        if (rect == null) {
            return null;
        }

        Point2D p0 = new Point2D.Double(rect.getMinX(), rect.getMinY());
        Point2D p1 = new Point2D.Double(rect.getMaxX(), rect.getMaxY());

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

    @Override
    public void applyTransform(AffineTransform transform) {
        transformChanging();
        localToParentTransform.preConcatenate(transform);
        transformChanged();
    }

    @Override
    public AffineTransform getTransform() {
        return getLocalToParentTransform();
    }

    public void setTransform(AffineTransform transform) {
        transformChanging();
        localToParentTransform.setTransform(transform);
        transformChanged();
    }

    @Override
    public void copyPosition(Movable src) {
        if (src instanceof VisualTransformableNode srcNode) {
            setRootSpacePosition(srcNode.getRootSpacePosition());
        }
    }

    @Override
    public void rotateClockwise() {
        for (Node node: getChildren()) {
            if (node instanceof Rotatable) {
                ((Rotatable) node).rotateClockwise();
            }
        }
    }

    @Override
    public void rotateCounterclockwise() {
        for (Node node: getChildren()) {
            if (node instanceof Rotatable) {
                ((Rotatable) node).rotateCounterclockwise();
            }
        }
    }

    @Override
    public void flipHorizontal() {
        for (Node node: getChildren()) {
            if (node instanceof Flippable) {
                ((Flippable) node).flipHorizontal();
            }
        }
    }

    @Override
    public void flipVertical() {
        for (Node node: getChildren()) {
            if (node instanceof Flippable) {
                ((Flippable) node).flipVertical();
            }
        }
    }

    public Collection<VisualComponent> getComponents() {
        return Hierarchy.getChildrenOfType(this, VisualComponent.class);
    }

    public Collection<VisualConnection> getConnections() {
        return Hierarchy.getChildrenOfType(this, VisualConnection.class);
    }

    public String getLabel() {
        return "";
    }

}
