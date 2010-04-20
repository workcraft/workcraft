/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.cpog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.util.Geometry;

public class VisualArc extends VisualConnection
{
	private final static Font labelFont = new Font("Century Schoolbook", Font.ITALIC, 1).deriveFont(0.5f);

	Arc mathConnection;

	public VisualArc(Arc mathConnection)
	{
		super();
		this.mathConnection = mathConnection;
	}

	public VisualArc(Arc mathConnection, VisualVertex first, VisualVertex second)
	{
		super(mathConnection, first, second);
		this.mathConnection = mathConnection;
	}

	public BooleanFormula getCondition()
	{
		return mathConnection.getCondition();
	}

	public void setCondition(BooleanFormula condition)
	{
		mathConnection.setCondition(condition);
		sendNotification(new PropertyChangedEvent(this, "condition"));
	}

	@Override
	public void draw(Graphics2D g)
	{
		if (getCondition() == One.instance()) return;

		FormulaRenderingResult result = FormulaToGraphics.render(getCondition(), g.getFontRenderContext(), labelFont);

		Rectangle2D labelBB = result.boundingBox;

		ConnectionGraphic graphic = getGraphic();

		Point2D p = graphic.getPointOnCurve(0.5);
		Point2D d = graphic.getDerivativeAt(0.5);
		Point2D dd = graphic.getSecondDerivativeAt(0.5);

		if (d.getX() < 0)
		{
			d = Geometry.multiply(d, -1);
			//dd = Geometry.multiply(dd, -1);
		}

		Point2D labelPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getMaxY());
		if (Geometry.crossProduct(d, dd) < 0) labelPosition.setLocation(labelPosition.getX(), labelBB.getMinY());

		AffineTransform transform = g.getTransform();
		g.translate(p.getX() - labelPosition.getX(), p.getY() - labelPosition.getY());
		g.transform(AffineTransform.getRotateInstance(d.getX(), d.getY(), labelPosition.getX(), labelPosition.getY()));

		g.setColor(Coloriser.colorise(Color.BLACK, getColorisation()));

		int k = 0;
		for(GlyphVector glyph : result.glyphs)
		{
			Point2D pos = result.glyphCoordinates.get(k++);
			g.drawGlyphVector(glyph, (float) pos.getX(), (float) pos.getY());
		}

		g.setStroke(new BasicStroke(0.025f));
		for(Line2D line : result.inversionLines) g.draw(line);

		g.setTransform(transform);


/* This code has a high artistic value and henceforth is permitted to stay here forever.
 *
 * 			d = Geometry.multiply(d, 0.1);
			dd = Geometry.multiply(dd, 0.02);

			Line2D l1 = new Line2D.Double(p, Geometry.add(p, d));
			Line2D l2 = new Line2D.Double(p, Geometry.add(p, dd));

			g.setStroke(new BasicStroke(0.02f));
			g.setColor(Color.BLUE);
			g.draw(l1);

			g.setColor(Color.ORANGE);
			g.draw(l2);
		}*/
	}
}
