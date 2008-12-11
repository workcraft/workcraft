package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualComponent;

public class VisualPlace extends VisualComponent {
	private static double size = 1;
	private static float strokeWidth = 0.1f;
	private static double singleTokenSize = size / 2;
	private static double multipleTokenSeparation = strokeWidth / 8;

	public VisualPlace(Place place) {
		super(place);
	}

	public VisualPlace(Place place, Element xmlElement) {
		super(place, xmlElement);
	}

	@Override
	public void draw(Graphics2D g)
	{
		Shape shape = new Ellipse2D.Double(
				getX() - size / 2 + strokeWidth / 2,
				getY() - size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);

		g.setColor(Color.WHITE);
		g.fill(shape);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(strokeWidth));
		g.draw(shape);

		Place p = (Place)getReferencedComponent();

		if (p.tokens == 1)
		{
			shape = new Ellipse2D.Double(
					getX() - singleTokenSize / 2,
					getY() - singleTokenSize / 2,
					singleTokenSize,
					singleTokenSize);

			g.setColor(Color.BLACK);
			g.fill(shape);
		}
		else
		if (p.tokens > 1 && p.tokens < 8)
		{
			double al = Math.PI / p.tokens;
			if (p.tokens == 7) al = Math.PI / 6;

			double r = (size / 2 - strokeWidth - multipleTokenSeparation) / (1 + 1 / Math.sin(al));
			double R = r / Math.sin(al);

			r -= multipleTokenSeparation;

			for(int i = 0; i < p.tokens; i++)
			{
				if (i == 6)
					shape = new Ellipse2D.Double(getX() - r, getY() - r, r * 2, r * 2);
				else
					shape = new Ellipse2D.Double(
							getX() - R * Math.sin(i * al * 2) - r,
							getY() - R * Math.cos(i * al * 2) - r,
							r * 2,
							r * 2);

				g.setColor(Color.BLACK);
				g.fill(shape);
			}
		}
		else if (p.tokens > 7)
		{
			String out = Integer.toString(p.tokens);
			Font superFont = g.getFont().deriveFont( (float)(size - multipleTokenSeparation - strokeWidth*2));

			Rectangle2D rect = superFont.getStringBounds(out, g.getFontRenderContext());
			g.setFont(superFont);


			g.drawString(Integer.toString(p.tokens), (float)(getX() - rect.getCenterX()), (float)(getY() - rect.getCenterY()));
		}
	}


	public Rectangle2D getBoundingBox() {
		return new Rectangle2D.Double(getX()-size/2, getY()-size/2, size, size);
	}
}
