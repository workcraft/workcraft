package org.workcraft.dom.visual;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class RenderedText implements RenderingResult {

    private static final FontRenderContext CONTEXT = new FontRenderContext(
            AffineTransform.getScaleInstance(1000.0, 1000.0), true, true);

    private final String text;
    private final Font font;
    private final Positioning positioning;
    private final double xOffset;
    private final double yOffset;
    private final GlyphVector glyphVector;
    private final Rectangle2D boundingBox;
    private final Point2D drawPosition;

    public RenderedText(String text, Font font, Positioning positioning, Point2D offset) {
        this.text = (text == null) ? "" : text.trim();
        this.font = font;
        this.positioning = positioning;
        this.xOffset = offset.getX();
        this.yOffset = offset.getY();

        glyphVector = font.createGlyphVector(CONTEXT, this.text);
        Rectangle2D textBox = glyphVector.getVisualBounds();
        double textWidth = textBox.getWidth();
        double textHeight = textBox.getHeight();
        double xCenterOffset = -0.5 * textWidth;
        double textAscend = -textBox.getY();
        double metricHeight = font.getLineMetrics(this.text, CONTEXT).getHeight();
        double yCenterOffset = -textAscend + 0.2 * metricHeight;

        double x = xOffset + positioning.xOffset + 0.5 * positioning.xSign * textWidth + xCenterOffset;
        double y = yOffset + positioning.yOffset + 0.2 * positioning.ySign * metricHeight + yCenterOffset;
        boundingBox = new Rectangle2D.Double(x, y, textWidth, textHeight);
        drawPosition = new Point2D.Double(x - textBox.getX(), y - textBox.getY());
    }

    public boolean isDifferent(String line, Font font, Positioning positioning, Point2D offset) {
        return !this.text.equals(line) || !this.font.equals(font) || (positioning != this.positioning)
                || (offset.getX() != this.xOffset) || (offset.getY() != this.yOffset);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setFont(font);
        g.drawGlyphVector(glyphVector, (float) drawPosition.getX(), (float) drawPosition.getY());
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }

    public Point2D getDrawPosition() {
        return drawPosition;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

}
