package org.workcraft.plugins.son.elements;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

@Hotkey(KeyEvent.VK_E)
@DisplayName ("Event")
@SVGIcon("images/son-node-event.svg")

public class VisualEvent extends VisualComponent implements VisualTransitionNode {

    private static double size = VisualCommonSettings.getNodeSize();
    private static double strokeWidth = VisualCommonSettings.getStrokeWidth();

    public VisualEvent(Event event) {
        super(event);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, "Fault",
                value -> getReferencedComponent().setFaulty(value),
                () -> getReferencedComponent().isFaulty())
                .setCombinable().setTemplatable());
    }

    @Override
    public Event getReferencedComponent() {
        return (Event) super.getReferencedComponent();
    }
    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        double xy = -size / 2 + strokeWidth / 2;
        double wh = size - strokeWidth;
        Shape shape = new Rectangle2D.Double(xy, xy, wh, wh);
        g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);
        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        g.setStroke(new BasicStroke((float) strokeWidth));
        g.draw(shape);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
        drawFault(r);
    }

    @Override
    public void drawFault(DrawRequest r) {
        if (SONSettings.isErrorTracing()) {
            Graphics2D g = r.getGraphics();
            GlyphVector glyphVector = null;
            Rectangle2D labelBB = null;

            Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(0.5f);

            if (isFaulty()) {
                glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "1");
            } else {
                glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "0");
            }

            labelBB = glyphVector.getVisualBounds();
            Point2D bitPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getCenterY());
            g.drawGlyphVector(glyphVector, -(float) bitPosition.getX(), -(float) bitPosition.getY());
        }
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return (Math.abs(pointInLocalSpace.getX()) <= 0.5 * size) && (Math.abs(pointInLocalSpace.getY()) <= 0.5 * size);
    }

    @Override
    public Event getMathTransitionNode() {
        return (Event) this.getReferencedComponent();
    }

    @Override
    public void setLabel(String label) {
        super.setLabel(label);
        ((Event) getReferencedComponent()).setLabel(label);
    }

    @Override
    public String getLabel() {
        super.getLabel();
        return ((Event) getReferencedComponent()).getLabel();
    }

    public void setFaulty(Boolean fault) {
        ((Event) getReferencedComponent()).setFaulty(fault);
    }

    @Override
    public boolean isFaulty() {
        return ((Event) getReferencedComponent()).isFaulty();
    }

    @Override
    public Color getForegroundColor() {
        return ((Event) getReferencedComponent()).getForegroundColor();
    }

    @Override
    public void setForegroundColor(Color foregroundColor) {
        ((Event) getReferencedComponent()).setForegroundColor(foregroundColor);
    }

    @Override
    public void setFillColor(Color fillColor) {
        ((Event) getReferencedComponent()).setFillColor(fillColor);
    }

    @Override
    public Color getFillColor() {
        return ((Event) getReferencedComponent()).getFillColor();
    }

    public void setStartTime(String time) {
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((Event) getReferencedComponent()).setStartTime(input);
    }

    public String getStartTime() {
        return ((Event) getReferencedComponent()).getStartTime().toString();
    }

    public void setEndTime(String time) {
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((Event) getReferencedComponent()).setEndTime(input);
    }

    public String getEndTime() {
        return ((Event) getReferencedComponent()).getEndTime().toString();
    }

    public void setDuration(String time) {
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((Event) getReferencedComponent()).setDuration(input);
    }

    public String getDuration() {
        return ((Event) getReferencedComponent()).getDuration().toString();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualEvent) {
            VisualEvent srcComponent = (VisualEvent) src;
            setFaulty(srcComponent.isFaulty());
        }
    }

}
