package org.workcraft.dom.visual;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class RenderedText {
	private GlyphVector glyphVector;
	private Rectangle2D textBB;
	private Font font;
	private float textX;
	private float textY;
	private String text;

	public RenderedText(Font font, String text) {
		this.font = font;
		this.text = text;
		update();
	}

	public void setText (String text)	{
		this.text = text;
		update();
	}

	public String getText() {
		return text;
	}

	private void update() {
		glyphVector = font.createGlyphVector(new FontRenderContext(AffineTransform.getScaleInstance(1000, 1000), true, true), text);
		textBB = glyphVector.getVisualBounds();

		double margin = 0.15;
		textBB = BoundingBoxHelper.expand(textBB, margin, margin);

		textX = (float)-textBB.getCenterX();
		textY = (float)-textBB.getCenterY();

		textBB.setRect(textBB.getX() - textBB.getCenterX(), textBB.getY() - textBB.getCenterY(), textBB.getWidth(), textBB.getHeight());
	}

	public void draw (Graphics2D g)
	{
		g.setFont(font);
		g.drawGlyphVector(glyphVector, textX, textY);
	}

	public Rectangle2D getBoundingBox()
	{
		return textBB;
	}
}
