package org.workcraft.dom.visual;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class TouchableRenderedText extends RenderedText implements Touchable {


	public TouchableRenderedText(String text, Font font, Positioning positioning, Point2D offset) {
		super(text, font, positioning, offset);
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
