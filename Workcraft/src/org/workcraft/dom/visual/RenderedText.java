package org.workcraft.dom.visual;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class RenderedText implements Touchable {
	final public String text;
	final public Font font;
	final private GlyphVector glyphVector;
	private Rectangle2D boundingBox;

	public RenderedText(String text, Font font) {
		this.text = text;
		this.font = font;
		final FontRenderContext context = new FontRenderContext(AffineTransform.getScaleInstance(1000, 1000), true, true);
		glyphVector = font.createGlyphVector(context, text);
		final Rectangle2D bb = BoundingBoxHelper.expand(glyphVector.getVisualBounds(), 0, 0);
		boundingBox = BoundingBoxHelper.move(bb, -bb.getCenterX(), -bb.getCenterY());
	}

	public void draw (Graphics2D g) {
		g.setFont(font);
		g.drawGlyphVector(glyphVector, (float)boundingBox.getMinX(), (float)boundingBox.getMaxY());
	}

	public void setCenter(double x, double y) {
		final Rectangle2D bb = BoundingBoxHelper.expand(glyphVector.getVisualBounds(), 0, 0);
		boundingBox = BoundingBoxHelper.move(bb, x - bb.getCenterX(), y - bb.getCenterY());
	}

	@Override
	public boolean hitTest(Point2D point) {
		return boundingBox.contains(point);
	}

	@Override
	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}

	@Override
	public Point2D getCenter() {
		return new Point2D.Double(boundingBox.getCenterX(), boundingBox.getCenterY());
	}
}
