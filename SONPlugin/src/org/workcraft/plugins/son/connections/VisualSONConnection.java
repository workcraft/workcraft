package org.workcraft.plugins.son.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.util.Geometry;

public class VisualSONConnection extends VisualConnection {

    public static final Font timeFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.35f);
    private RenderedText timeRenderedText = new RenderedText("", timeFont, Positioning.CENTER, new Point2D.Double());

    public VisualSONConnection() {
        this(null, null, null);
    }

    public VisualSONConnection(SONConnection refConnection) {
        this(refConnection, null, null);
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualSONConnection, String>(
                this, "Semantic", String.class, true, true, true) {
            public void setter(VisualSONConnection object, String value) {
            }
            public String getter(VisualSONConnection object) {
                switch (getSemantics()) {
                case PNLINE:
                    return "Petri-net connection";
                case SYNCLINE:
                    return "Synchronous communication";
                case ASYNLINE:
                    return "Asynchronous communication";
                case BHVLINE:
                    return "Behavioural abstraction";
                default:
                    return getSemantics().toString();
                }
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualSONConnection, Color>(
                this, "Time color", Color.class, true, true, true) {
            protected void setter(VisualSONConnection object, Color value) {
                object.setTimeLabelColor(value);
            }
            protected Color getter(VisualSONConnection object) {
                return object.getTimeLabelColor();
            }
        });

    }

    public VisualSONConnection(SONConnection refConnection, VisualComponent first, VisualComponent second) {
        super(refConnection, first, second);
        addPropertyDeclarations();
        removePropertyDeclarationByName(VisualConnection.PROPERTY_LINE_WIDTH);
        removePropertyDeclarationByName(VisualConnection.PROPERTY_ARROW_WIDTH);
        removePropertyDeclarationByName(VisualConnection.PROPERTY_ARROW_LENGTH);
    }

    public SONConnection getReferencedSONConnection() {
        return (SONConnection) getReferencedConnection();
    }

    public Semantics getSemantics() {
        return getReferencedSONConnection().getSemantics();
    }

    public void setSemantics(Semantics semantics) {
        getReferencedSONConnection().setSemantics(semantics);
        invalidate();
    }

    @Override
    public Stroke getStroke() {
        switch (getSemantics()) {
        case SYNCLINE:
        case ASYNLINE:
            return new BasicStroke(0.15f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.5f, new float[]{0.1f, 0.075f}, 0f);
        case BHVLINE:
            return new BasicStroke(0.02f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 2.0f, new float[]{0.24f, 0.15f}, 0f);
        default:
            return super.getStroke();
        }
    }

    @Override
    public double getArrowWidth() {
        if (getSemantics() == Semantics.ASYNLINE) {
            return 0.3;
        }
        return super.getArrowWidth();
    }

    @Override
    public double getArrowLength() {
        if (getSemantics() == Semantics.ASYNLINE) {
            return 0.7;
        }
        return super.getArrowLength();
    }

    @Override
    public boolean hasArrow() {
        if (getSemantics() == Semantics.SYNCLINE) {
            return false;
        }
        return super.hasArrow();
    }

    public boolean getLabelVisibility() {
        return SONSettings.getTimeVisibility();
    }

    protected void cacheLabelRenderedText(DrawRequest r) {
        String time = "T: " + getTime().toString();

        if (timeRenderedText.isDifferent(time, timeFont, Positioning.CENTER, new Point2D.Double())) {
            timeRenderedText = new RenderedText(time, timeFont, Positioning.CENTER, new Point2D.Double());
        }
    }

    private AffineTransform getLabelTransform() {
        ConnectionGraphic graphic = getGraphic();
        Point2D middlePoint = graphic.getPointOnCurve(0.5);
        Point2D firstDerivative = graphic.getDerivativeAt(0.5);
        Point2D secondDerivative = graphic.getSecondDerivativeAt(0.5);
        if (firstDerivative.getX() < 0) {
            firstDerivative = Geometry.multiply(firstDerivative, -1);
        }

        Rectangle2D bb = timeRenderedText.getBoundingBox();
        Point2D labelPosition = new Point2D.Double(bb.getCenterX(), bb.getMaxY());
        if (Geometry.crossProduct(firstDerivative, secondDerivative) < 0) {
            labelPosition.setLocation(labelPosition.getX(), bb.getMinY());
        }

        AffineTransform transform = AffineTransform.getTranslateInstance(
                middlePoint.getX() - labelPosition.getX(), middlePoint.getY() - labelPosition.getY());
        AffineTransform rotateTransform = AffineTransform.getRotateInstance(
                firstDerivative.getX(), firstDerivative.getY(), labelPosition.getX(), labelPosition.getY());
        transform.concatenate(rotateTransform);
        return transform;
    }

    protected void drawLabelInLocalSpace(DrawRequest r) {
        if (getLabelVisibility() && getReferencedSONConnection().getTime().isSpecified()) {
            cacheLabelRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();

            AffineTransform oldTransform = g.getTransform();
            AffineTransform transform = getLabelTransform();
            g.transform(transform);
            g.setColor(Coloriser.colorise(getTimeLabelColor(), d.getColorisation()));
            timeRenderedText.draw(g);
            g.setTransform(oldTransform);
        }
    }

    @Override
    public void draw(DrawRequest r) {
        if (getSemantics() == Semantics.PNLINE || getSemantics() == Semantics.ASYNLINE)
            drawLabelInLocalSpace(r);
    }

    private Rectangle2D getLabelBoundingBox() {
        return BoundingBoxHelper.transform(timeRenderedText.getBoundingBox(), getLabelTransform());
    }

    @Override
    public Rectangle2D getBoundingBox() {
        Rectangle2D labelBB = getLabelBoundingBox();
        return BoundingBoxHelper.union(super.getBoundingBox(), labelBB);
    }

    @Override
    public boolean hitTest(Point2D pointInParentSpace) {
        Rectangle2D labelBB = getLabelBoundingBox();
        if (labelBB != null && labelBB.contains(pointInParentSpace)) return true;
        return super.hitTest(pointInParentSpace);
    }

    @Override
    public Color getColor() {
        return getReferencedSONConnection().getColor();
    }

    @Override
    public void setColor(Color color) {
        getReferencedSONConnection().setColor(color);
    }

    public String getTime() {
        return getReferencedSONConnection().getTime().toString();
    }

    public void setTime(String time) {
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        getReferencedSONConnection().setTime(input);
    }

    public Color getTimeLabelColor() {
        return getReferencedSONConnection().getTimeLabelColor();
    }

    public void setTimeLabelColor(Color value) {
        getReferencedSONConnection().setTimeLabelColor(value);
    }
}
