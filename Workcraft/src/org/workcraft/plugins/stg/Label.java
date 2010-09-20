package org.workcraft.plugins.stg;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

public class Label {
	private GlyphVector glyphVector;
	private Rectangle2D textBB;
	private Font font;
	private float textX;
	private float textY;

	public Label(Font font, String text) {
		this.font = font;
		setText(text);
	}

	public void setText (String text)
	{
		glyphVector = font.createGlyphVector(new FontRenderContext(null, true, true), text);
		textBB = glyphVector.getVisualBounds();

		textBB.setRect(textBB.getX() - 0.075, textBB.getY() - 0.075, textBB.getWidth() + 0.15, textBB.getHeight() + 0.15);

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