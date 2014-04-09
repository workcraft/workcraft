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
	final public Positioning positioning;
	final public double xOffset;
	final public double yOffset;
	final private GlyphVector glyphVector;
	private Rectangle2D boundingBox;

	public RenderedText(String text, Font font, Positioning positioning, Point2D offset) {
		this.text = text;
		this.font = font;
		this.positioning = positioning;
		this.xOffset = offset.getX();
		this.yOffset = offset.getY();
		final FontRenderContext context = new FontRenderContext(AffineTransform.getScaleInstance(1000, 1000), true, true);
		glyphVector = font.createGlyphVector(context, text);
		final Rectangle2D bb = glyphVector.getVisualBounds();
		double x = xOffset + positioning.xOffset + 0.5 * positioning.xSign * bb.getWidth();
		double y = yOffset + positioning.yOffset + 0.5 * positioning.ySign * bb.getHeight();
		boundingBox = BoundingBoxHelper.move(bb, x - bb.getCenterX(), y - bb.getCenterY());
	}

	public boolean isDifferent(String text, Font font, Positioning positioning, Point2D offset) {
		if (text == null) {
			text = "";
		}
		return (!text.equals(this.text) || !font.equals(this.font) || positioning != this.positioning
				|| offset.getX() != this.xOffset || offset.getY() != this.yOffset);
	}

	public void draw (Graphics2D g) {
		g.setFont(font);
		g.drawGlyphVector(glyphVector, (float)boundingBox.getMinX(), (float)boundingBox.getMaxY());
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
