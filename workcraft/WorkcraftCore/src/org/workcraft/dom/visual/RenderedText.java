package org.workcraft.dom.visual;

import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

public class RenderedText {

    private static final double MIN_LINE_HEIGHT = 0.35;

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
        glyphVectors = new LinkedList<>();
        String[] lines = {""};
        if (text != null) {
            lines = text.split("\\|");
        }
        for (String line : lines) {
            final FontRenderContext context = new FontRenderContext(AffineTransform.getScaleInstance(1000.0, 1000.0), true, true);
            final GlyphVector glyphVector = font.createGlyphVector(context, line.trim());
            glyphVectors.add(glyphVector);
            Rectangle2D lineBox = glyphVector.getVisualBounds();
            if (textBounds != null) {
                textBounds = BoundingBoxHelper.move(textBounds, 0.0, -Math.max(lineBox.getHeight(), MIN_LINE_HEIGHT));
            }
            textBounds = BoundingBoxHelper.union(textBounds, lineBox);
        }
        if (textBounds == null) {
            textBounds = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
        }
        int lineCount = lines.length;
        double spacingRatio = VisualCommonSettings.getLineSpacing();
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
        for (GlyphVector glyphVector : glyphVectors) {
            final Rectangle2D lineBox = glyphVector.getVisualBounds();
            double xMargin;
            switch (alignment) {
            case CENTER :
                xMargin = (boundingBox.getWidth() - lineBox.getWidth()) / 2.0;
                break;
            case RIGHT:
                xMargin = boundingBox.getWidth() - lineBox.getWidth();
                break;
            default:
                xMargin = 0.0;
                break;
            }
            double x = boundingBox.getX() - lineBox.getX() + xMargin * (1.0 - positioning.xSign);
            y += Math.max(lineBox.getHeight(), MIN_LINE_HEIGHT);
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
