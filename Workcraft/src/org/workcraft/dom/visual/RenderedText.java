package org.workcraft.dom.visual;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class RenderedText implements Touchable {
	final private static double margin = 0.0;
	final private GlyphVector glyphVector;
	final private Rectangle2D boundingBox;
	final public Font font;
	final public String text;
	final public float x;
	final public float y;

	public RenderedText(Font font, String text) {
		this.font = font;
		this.text = text;
		glyphVector = font.createGlyphVector(new FontRenderContext(AffineTransform.getScaleInstance(1000, 1000), true, true), text);
		Rectangle2D bb = BoundingBoxHelper.expand(glyphVector.getVisualBounds(), margin, margin);
		x = (float)-bb.getCenterX();
		y = (float)-bb.getCenterY();
		boundingBox = BoundingBoxHelper.move(bb, -bb.getCenterX(), -bb.getCenterY());
	}

	public void draw (Graphics2D g) {
		g.setFont(font);
//!!!		g.drawGlyphVector(glyphVector, (float)boundingBox.getX(), (float)boundingBox.getY());
		g.drawGlyphVector(glyphVector, x, y);
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
