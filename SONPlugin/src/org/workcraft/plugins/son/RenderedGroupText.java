package org.workcraft.plugins.son;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class RenderedGroupText {
	private final double spacingRatio = CommonVisualSettings.getLineSpacing();
	private final String text;
	private final Font font;
	private final double xOffset;
	private final double yOffset;
	private final double margin = 0.35f;
	private final LinkedList<GlyphVector> glyphVectors;
	private final Rectangle2D boundingBox;
	private final double spacing;

	public RenderedGroupText(String text, Font font, Point2D offset) {
		this.text = text;
		this.font = font;
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
		double x = xOffset - textBounds.getMaxX();
		double y = yOffset - margin - 0.5 * textBounds.getHeight() - textBounds.getCenterY();
		boundingBox = BoundingBoxHelper.move(textBounds, x, y);
	}

	public boolean isDifferent(String text, Font font, Point2D offset) {
		if (text == null) {
			text = "";
		}
		return (!text.equals(this.text) || !font.equals(this.font)
				|| offset.getX() != this.xOffset || offset.getY() != this.yOffset);
	}

	public void draw (Graphics2D g) {
		g.setFont(font);
		float y = (float)boundingBox.getMinY();
		for (GlyphVector glyphVector: glyphVectors) {
			final Rectangle2D lineBoundingBox = glyphVector.getVisualBounds();
			y += lineBoundingBox.getHeight();
			g.drawGlyphVector(glyphVector, (float)boundingBox.getX(), y);
			y += spacing;
		}
	}

	public boolean hitTest(Point2D point) {
		return boundingBox.contains(point);
	}

	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}
}
