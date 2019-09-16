package org.workcraft.plugins.xbm;

import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.builtin.settings.CommonEditorSettings;
import org.workcraft.plugins.builtin.settings.CommonSignalSettings;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.utils.Coloriser;
import org.workcraft.utils.Geometry;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

public class VisualBurstEvent extends VisualEvent {

    private RenderedText labelRenderedText = new RenderedText("", labelFont, Positioning.CENTER, new Point2D.Double());
    private Color labelColor = CommonVisualSettings.getLabelColor();

    private static final String BURST_SPLIT_SYMBOL = "/";
    private static final double LABEL_X_POSITION_THRESHOLD = 0;
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

    public BurstEvent getReferencedBurstEvent() {
        return (BurstEvent) getReferencedEvent();
    }

    private Burst getReferencedBurst() {
        return getReferencedBurstEvent().getBurst();
    }

    private Set<XbmSignal> getReferencedSignals() {
        return getReferencedBurst().getSignals();
    }

    @Override
    public void draw(DrawRequest r) {
        String input = "";
        String output = "";
        if (getReferencedBurstEvent().hasConditional()) {
            input += "<" + getReferencedBurstEvent().getConditional() + "> ";
        }
        input += getReferencedBurstEvent().getInputBurstString();
        output += getReferencedBurstEvent().getOutputBurstString();
        if (input.isEmpty() && output.isEmpty()) {
            super.draw(r);
        } else {
            cacheLabelRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            AffineTransform oldTransform = g.getTransform();
            AffineTransform transform = getLabelTransform();
            g.transform(transform);
            RenderedText inputText = new RenderedText(input, labelFont, Positioning.CENTER, new Point2D.Double());
            RenderedText outputText = new RenderedText(output, labelFont, Positioning.CENTER, new Point2D.Double());
            RenderedText burstSplitText = new RenderedText(BURST_SPLIT_SYMBOL, labelFont, Positioning.CENTER, new Point2D.Double());
            Color background = d.getBackground();
            if (background != null) {
                g.setColor(Coloriser.colorise(CommonEditorSettings.getBackgroundColor(), background));
                Rectangle2D box = BoundingBoxHelper.expand(labelRenderedText.getBoundingBox(), 0.2, 0.0);
                g.fill(box);
            }
            double inputTextWidth = inputText.getBoundingBox().getWidth() / 2;
            double outputTextWidth = outputText.getBoundingBox().getWidth() / 2;
            double burstSplitTextWidth = burstSplitText.getBoundingBox().getWidth();
            double burstSplitBoundary = burstSplitTextWidth + 0.2 / 2;

            //Draw input burst first
            g.setColor(Coloriser.colorise(CommonSignalSettings.getInputColor(), d.getColorisation()));
            g.translate(-(inputTextWidth + burstSplitBoundary), -LABEL_Y_POSITION_THRESHOLD);
            inputText.draw(g);
            g.translate(inputTextWidth + burstSplitBoundary, 0);

            //Draw '/' symbol next
            g.setColor(Coloriser.colorise(labelColor, d.getColorisation()));
            burstSplitText.draw(g);

            //Draw output burst last
            g.setColor(Coloriser.colorise(CommonSignalSettings.getOutputColor(), d.getColorisation()));
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
        if (getReferencedBurstEvent().getAsString().isEmpty()) {
            label = getReferencedBurstEvent().getAsString();
        }
        return label;
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualBurstEvent) {
            VisualBurstEvent srcBurstEvent = (VisualBurstEvent) src;
            getReferencedBurstEvent();
        }
    }
}