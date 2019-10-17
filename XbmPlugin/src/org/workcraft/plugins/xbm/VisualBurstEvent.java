package org.workcraft.plugins.xbm;

import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.utils.Coloriser;
import org.workcraft.utils.Geometry;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VisualBurstEvent extends VisualEvent {

    private final RenderedText labelRenderedText = new RenderedText("", getLabelFont(), Positioning.CENTER, new Point2D.Double());
    private final Color labelColor = VisualCommonSettings.getLabelColor();

    private static final String BURST_SPLIT_SYMBOL = "/";
    private static final double LABEL_Y_POSITION_THRESHOLD = 0.25;

    public VisualBurstEvent() {
        this(null, null, null);
    }

    public VisualBurstEvent(BurstEvent mathConnection) {
        this(mathConnection, null, null);
    }

    public VisualBurstEvent(BurstEvent mathConnection, VisualState first, VisualState second) {
        super(mathConnection, first, second);
    }

    @Override
    public BurstEvent getReferencedConnection() {
        return (BurstEvent) super.getReferencedConnection();
    }

    @Override
    public void draw(DrawRequest r) {
        String input = "";
        String output = "";
        if (getReferencedConnection().hasConditional()) {
            input += "<" + getReferencedConnection().getConditional() + "> ";
        }
        input += getReferencedConnection().getInputBurstString();
        output += getReferencedConnection().getOutputBurstString();
        if (input.isEmpty() && output.isEmpty()) {
            super.draw(r);
        } else {
            cacheLabelRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            AffineTransform oldTransform = g.getTransform();
            AffineTransform transform = getLabelTransform();
            g.transform(transform);
            RenderedText inputText = new RenderedText(input, getLabelFont(), Positioning.CENTER, new Point2D.Double());
            RenderedText outputText = new RenderedText(output, getLabelFont(), Positioning.CENTER, new Point2D.Double());
            RenderedText burstSplitText = new RenderedText(BURST_SPLIT_SYMBOL, getLabelFont(), Positioning.CENTER, new Point2D.Double());
            Color background = d.getBackground();
            if (background != null) {
                g.setColor(Coloriser.colorise(EditorCommonSettings.getBackgroundColor(), background));
                Rectangle2D box = BoundingBoxHelper.expand(labelRenderedText.getBoundingBox(), 0.2, 0.0);
                g.fill(box);
            }
            double inputTextWidth = inputText.getBoundingBox().getWidth() / 2;
            double outputTextWidth = outputText.getBoundingBox().getWidth() / 2;
            double burstSplitTextWidth = burstSplitText.getBoundingBox().getWidth();
            double burstSplitBoundary = burstSplitTextWidth + 0.2 / 2;

            //Draw input burst first
            g.setColor(Coloriser.colorise(SignalCommonSettings.getInputColor(), d.getColorisation()));
            g.translate(-(inputTextWidth + burstSplitBoundary), -LABEL_Y_POSITION_THRESHOLD);
            inputText.draw(g);
            g.translate(inputTextWidth + burstSplitBoundary, 0);

            //Draw '/' symbol next
            g.setColor(Coloriser.colorise(labelColor, d.getColorisation()));
            burstSplitText.draw(g);

            //Draw output burst last
            g.setColor(Coloriser.colorise(SignalCommonSettings.getOutputColor(), d.getColorisation()));
            g.translate(outputTextWidth + burstSplitBoundary, 0);
            outputText.draw(g);
            g.translate(-(outputTextWidth + burstSplitBoundary), LABEL_Y_POSITION_THRESHOLD);

            g.setTransform(oldTransform);
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

    @Override
    public String getLabel(DrawRequest r) {
        String label = Character.toString(EPSILON_SYMBOL);
        if (getReferencedConnection().getAsString().isEmpty()) {
            label = getReferencedConnection().getAsString();
        }
        return label;
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualBurstEvent) {
            getReferencedConnection();
        }
    }
}