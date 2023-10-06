package org.workcraft.dom.visual;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

public class RenderedParagraph {

    private static final FontRenderContext CONTEXT = new FontRenderContext(
            AffineTransform.getScaleInstance(1000.0, 1000.0), true, true);

    private final String text;
    private final Font font;
    private final Positioning positioning;
    private final double xOffset;
    private final double yOffset;
    private final LinkedList<GlyphVector> glyphVectors;
    private final Rectangle2D boundingBox;
    private final float lineMetricsHeight;

    public RenderedParagraph(String text, Font font, Positioning positioning, Point2D offset) {
        this.text = (text == null) ? "" : text.trim();
        this.font = font;
        this.positioning = positioning;
        xOffset = offset.getX();
        yOffset = offset.getY();
        lineMetricsHeight = font.getLineMetrics(text, CONTEXT).getHeight();
        glyphVectors = new LinkedList<>();
        double boxWidth = 0.0;
        double boxHeight = 0.0;
        for (String line : this.text.split("\\|")) {
            GlyphVector glyphVector = font.createGlyphVector(CONTEXT, line.trim());
            glyphVectors.add(glyphVector);
            boxWidth = Math.max(glyphVector.getVisualBounds().getWidth(), boxWidth);
            boxHeight += lineMetricsHeight;
        }
        double x = xOffset + positioning.xOffset + 0.5 * (positioning.xSign - 1) * boxWidth;
        double y = yOffset + positioning.yOffset + 0.5 * (positioning.ySign - 1) * boxHeight;
        boundingBox = new Rectangle2D.Double(x, y, boxWidth, boxHeight);
    }

    public boolean isDifferent(String text, Font font, Positioning positioning, Point2D offset) {
        return !this.text.equals(text) || !this.font.equals(font) || positioning != this.positioning
                || offset.getX() != this.xOffset || offset.getY() != this.yOffset;
    }

    public void draw(Graphics2D g, Alignment alignment) {
        g.setFont(font);
        float y = (float) boundingBox.getMinY() + 0.8f * lineMetricsHeight;
        for (GlyphVector glyphVector : glyphVectors) {
            Rectangle2D lineBox = glyphVector.getVisualBounds();
            double xMargin = 0.0;
            if (alignment == Alignment.CENTER) {
                xMargin = (boundingBox.getWidth() - lineBox.getWidth()) / 2.0;
            }
            if (alignment == Alignment.RIGHT) {
                xMargin = boundingBox.getWidth() - lineBox.getWidth();
            }
            float x = (float) (boundingBox.getX() - lineBox.getX() + xMargin * (1.0 - positioning.xSign));
            g.drawGlyphVector(glyphVector, x, y);
            y += lineMetricsHeight;
        }
    }

    public boolean hitTest(Point2D point) {
        return boundingBox.contains(point);
    }

    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }

    public boolean isEmpty() {
        return (text == null) || text.isEmpty();
    }

}
