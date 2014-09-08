package org.workcraft.plugins.son;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;

public class RenderedGroupText extends RenderedText{

	private final double xOffset;
	private final double yOffset;
	private double margin = 0.10f;

	public RenderedGroupText(String text, Font font, Positioning positioning,
			Point2D offset) {
		super(text, font, positioning, offset);
		this.xOffset = offset.getX();
		this.yOffset = offset.getY();

		Rectangle2D textBounds = null;
		glyphVectors = new LinkedList<GlyphVector>();
		String[] lines = text.split("\\|");
		for (String line: lines) {
			final FontRenderContext context = new FontRenderContext(AffineTransform.getScaleInstance(1000, 1000), true, true);
			final GlyphVector glyphVector = font.createGlyphVector(context, line.trim());
			glyphVectors.add(glyphVector);
			Rectangle2D lineBounds = glyphVector.getVisualBounds();
			if (textBounds != null) {
				textBounds = BoundingBoxHelper.move(textBounds, 0.0, -lineBounds.getHeight());
			}
			textBounds = BoundingBoxHelper.union(textBounds, lineBounds);
		}
		spacing = (lines.length < 2) ? 0.0 : (spacingRatio * textBounds.getHeight() / (lines.length - 1));
		textBounds = BoundingBoxHelper.transform(textBounds, AffineTransform.getScaleInstance(1.0, 1.0 + spacingRatio));
		double x = xOffset + positioning.xOffset - margin - 0.5 * positioning.xSign * textBounds.getWidth() - textBounds.getCenterX();
		double y = yOffset + positioning.yOffset - margin + 0.5 * positioning.ySign * textBounds.getHeight() - textBounds.getCenterY();
		boundingBox = BoundingBoxHelper.move(textBounds, x, y);
	}
}
