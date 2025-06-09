package org.workcraft.plugins.fsm;

import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.Geometry;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VisualEvent extends VisualConnection {

    public static final String EPSILON_SYMBOL = Character.toString((char) 0x03B5);

    public static final String PROPERTY_LABEL_COLOR = "Label color";

    public static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 1);

    private RenderedText labelRenderedText = new RenderedText("", getLabelFont(), Positioning.CENTER, new Point2D.Double());
    private Color labelColor = VisualCommonSettings.getLabelColor();

    public VisualEvent() {
        this(null, null, null);
    }

    public VisualEvent(Event mathConnection) {
        this(mathConnection, null, null);
    }

    public VisualEvent(Event mathConnection, VisualState first, VisualState second) {
        super(mathConnection, first, second);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Color.class, PROPERTY_LABEL_COLOR,
                this::setLabelColor, this::getLabelColor).setCombinable().setTemplatable());
    }

    @Override
    public Event getReferencedConnection() {
        return (Event) super.getReferencedConnection();
    }

    public boolean getLabelVisibility() {
        return true;
    }

    public Font getLabelFont() {
        return LABEL_FONT.deriveFont((float) VisualCommonSettings.getLabelFontSize());
    }

    protected void cacheLabelRenderedText(DrawRequest r) {
        cacheLabelRenderedText(getLabel(r), getLabelFont(), Positioning.CENTER, new Point2D.Double());
    }

    protected void cacheLabelRenderedText(String text, Font font, Positioning positioning, Point2D offset) {
        if (labelRenderedText.isDifferent(text, font, positioning, offset)) {
            labelRenderedText = new RenderedText(text, font, positioning, offset);
        }
    }

    public String getLabel(DrawRequest r) {
        String label = EPSILON_SYMBOL;
        Symbol symbol = getReferencedConnection().getSymbol();
        if (symbol != null) {
            label = r.getModel().getMathName(symbol);
        }
        return label;
    }

    private AffineTransform getLabelTransform() {
        ConnectionGraphic graphic = getGraphic();
        Point2D middlePoint = graphic.getPointOnCurve(0.5);
        Point2D firstDerivative = graphic.getDerivativeAt(0.5);
        Point2D secondDerivative = graphic.getSecondDerivativeAt(0.5);
        if (firstDerivative.getX() < 0) {
            firstDerivative = Geometry.multiply(firstDerivative, -1);
        }

        Rectangle2D bb = labelRenderedText.getBoundingBox();
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
        if (getLabelVisibility()) {
            cacheLabelRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();

            AffineTransform oldTransform = g.getTransform();
            AffineTransform transform = getLabelTransform();
            g.transform(transform);
            Color background = d.getBackground();
            if (background != null) {
                g.setColor(ColorUtils.colorise(EditorCommonSettings.getBackgroundColor(), background));
                Rectangle2D box = BoundingBoxHelper.expand(labelRenderedText.getBoundingBox(), 0.2, 0.0);
                g.fill(box);
            }
            g.setColor(ColorUtils.colorise(getLabelColor(), d.getColorisation()));
            labelRenderedText.draw(g);
            g.setTransform(oldTransform);
        }
    }

    @Override
    public void draw(DrawRequest r) {
        drawLabelInLocalSpace(r);
    }

    private Rectangle2D getLabelBoundingBox() {
        return BoundingBoxHelper.transform(labelRenderedText.getBoundingBox(), getLabelTransform());
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

    public Color getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(Color value) {
        if (!labelColor.equals(value)) {
            labelColor = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_LABEL_COLOR));
        }
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualEvent srcEvent) {
            setLabelColor(srcEvent.getLabelColor());
            // Note: symbol should not be copied as it may break compatibility for derived types
        }
    }

}
