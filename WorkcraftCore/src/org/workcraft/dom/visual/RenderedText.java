package org.workcraft.dom.visual;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import org.workcraft.plugins.builtin.settings.CommonVisualSettings;

public class RenderedText {
    private final double spacingRatio = CommonVisualSettings.getLineSpacing();
    private final String text;
    private final Font font;
    private final Positioning positioning;
    private final double xOffset;
    private final double yOffset;
    private final LinkedList<GlyphVector> glyphVectors;
    private final Rectangle2D boundingBox;
    private final double spacing;

    public RenderedText(String text, Font font, Positioning positioning, Point2D offset) {
        this.text = text;
        this.font = font;
        this.positioning = positioning;
        this.xOffset = offset.getX();
        this.yOffset = offset.getY();

        Rectangle2D textBounds = null;
        glyphVectors = new LinkedList<GlyphVector>();
        String[] lines = {""};
        if (text != null) {
            lines = text.split("\\|");
        }
        for (String line: lines) {
            final FontRenderContext context = new FontRenderContext(AffineTransform.getScaleInstance(1000.0, 1000.0), true, true);
            final GlyphVector glyphVector = font.createGlyphVector(context, line.trim());
            glyphVectors.add(glyphVector);
            Rectangle2D lineBounds = glyphVector.getVisualBounds();
            if (textBounds != null) {
                textBounds = BoundingBoxHelper.move(textBounds, 0.0, -lineBounds.getHeight());
            }
            textBounds = BoundingBoxHelper.union(textBounds, lineBounds);
        }
        int lineCount = lines.length;
        spacing = (lineCount < 2) ? 0.0 : (spacingRatio * textBounds.getHeight() / (lineCount - 1));
        textBounds = BoundingBoxHelper.transform(textBounds, AffineTransform.getScaleInstance(1.0, 1.0 + spacingRatio));
        double x = xOffset + positioning.xOffset + 0.5 * positioning.xSign * textBounds.getWidth() - textBounds.getCenterX();
        double y = yOffset + positioning.yOffset + 0.5 * positioning.ySign * textBounds.getHeight() - textBounds.getCenterY();
        boundingBox = BoundingBoxHelper.move(textBounds, x, y);
    }

    public boolean isDifferent(String text, Font font, Positioning positioning, Point2D offset) {
        if (text == null) {
            text = "";
        }
        return !text.equals(this.text) || !font.equals(this.font) || positioning != this.positioning
                || offset.getX() != this.xOffset || offset.getY() != this.yOffset;
    }

    public void draw(Graphics2D g) {
        draw(g, Alignment.LEFT);
    }

    public void draw(Graphics2D g, Alignment alignment) {
        g.setFont(font);
        float y = (float) boundingBox.getMinY();
        for (GlyphVector glyphVector: glyphVectors) {
            final Rectangle2D lineBoundingBox = glyphVector.getVisualBounds();
            double xMargin = 0.0;
            switch (alignment) {
            case CENTER :
                xMargin = (boundingBox.getWidth() - lineBoundingBox.getWidth()) / 2.0;
                break;
            case RIGHT:
                xMargin = boundingBox.getWidth() - lineBoundingBox.getWidth();
                break;
            default:
                xMargin = 0.0;
            }
            double x = boundingBox.getX() - lineBoundingBox.getX() + xMargin * (1.0 - positioning.xSign);
            y += lineBoundingBox.getHeight();
            g.drawGlyphVector(glyphVector, (float) x, y);
            y += spacing;
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
