package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import java.awt.font.GlyphVector;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class FormulaRenderingResult
{
	public Rectangle2D boundingBox = null;

	public double visualTop = 0.0;

	public List<GlyphVector> glyphs = null;
	public List<Point2D> glyphCoordinates = null;
	public List<Line2D> inversionLines = null;

	public void add(FormulaRenderingResult summand)
	{
		for(GlyphVector glyph : summand.glyphs) glyphs.add(glyph);
		for(Point2D p : summand.glyphCoordinates)
			glyphCoordinates.add(new Point2D.Double(
					p.getX() + boundingBox.getWidth(),
					p.getY()));

		for(Line2D line : summand.inversionLines)
			inversionLines.add(new Line2D.Double(
					line.getX1() + boundingBox.getWidth(), line.getY1(),
					line.getX2() + boundingBox.getWidth(), line.getY2()));

		boundingBox.add(new Point2D.Double(
				boundingBox.getMaxX() + summand.boundingBox.getWidth(),
				summand.boundingBox.getMinY()));

		visualTop = Math.min(visualTop, summand.visualTop);
	}
}
