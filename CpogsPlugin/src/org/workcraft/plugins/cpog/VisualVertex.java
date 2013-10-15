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
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;

import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.expressions.CpogFormulaVariable;
import org.workcraft.plugins.cpog.expressions.CpogVisitor;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

@Hotkey(KeyEvent.VK_V)
@SVGIcon("images/icons/svg/vertex.svg")
public class VisualVertex extends VisualComponent implements CpogFormulaVariable
{
	private final static double size = 1;
	private final static float strokeWidth = 0.1f;
	private static Font labelFont;
	private Point2D labelPosition = null;
	private Rectangle2D labelBoundingBox = null;

	static {
		try {
			Font font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb"));
			labelFont = font.deriveFont(0.5f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public VisualVertex(Vertex vertex) {
		super(vertex);
	}

	public void draw(DrawRequest r)
	{
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();

		Shape shape = new Ellipse2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2,
				size - strokeWidth, size - strokeWidth);

		BooleanFormula value = evaluate();

		g.setColor(Coloriser.colorise(getFillColor(), colorisation));
		g.fill(shape);

		g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
		if (value == Zero.instance())
		{
			g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT,
			        BasicStroke.JOIN_MITER, 1.0f, new float[] {0.18f, 0.18f}, 0.00f));
		}
		else
		{
			g.setStroke(new BasicStroke(strokeWidth));
			if (value != One.instance())
				g.setColor(Coloriser.colorise(Color.LIGHT_GRAY, colorisation));
		}

		g.draw(shape);
		drawLabelInLocalSpace(r);
	}

	protected void drawLabelInLocalSpace(DrawRequest r)
	{
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();

		String text = getLabel();
		if (getCondition() != One.instance()) text += ": ";
		FormulaRenderingResult result = FormulaToGraphics.print(text, labelFont, g.getFontRenderContext());
		if (getCondition() != One.instance()) result.add(FormulaToGraphics.render(getCondition(), g.getFontRenderContext(), labelFont));

		Rectangle2D gbb = result.boundingBox;
		labelPosition = new Point2D.Double(
			-gbb.getCenterX() + 0.5 * getLabelPositioning().xOffset * (1.0 + gbb.getWidth() + 0.2),
			-gbb.getCenterY() + 0.5 * getLabelPositioning().yOffset * (1.0 + gbb.getHeight() + 0.2));
		labelBoundingBox = new Rectangle2D.Double(gbb.getX() + labelPosition.getX(), gbb.getY() + labelPosition.getY(),
				gbb.getWidth(), gbb.getHeight());

		AffineTransform oldTransform = g.getTransform();
		g.translate(labelPosition.getX(), labelPosition.getY());
		result.draw(g, Coloriser.colorise(getLabelColor(), colorisation));
		g.setTransform(oldTransform);
	}

	public Rectangle2D getBoundingBoxInLocalSpace()
	{
		Rectangle2D bb = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		return BoundingBoxHelper.union(bb, labelBoundingBox);
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
		return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
	}

	public Vertex getMathVertex()
	{
		return (Vertex) getReferencedComponent();
	}

	public BooleanFormula getCondition()
	{
		return getMathVertex().getCondition();
	}

	public void setCondition(BooleanFormula condition)
	{
		getMathVertex().setCondition(condition);
		sendNotification(new PropertyChangedEvent(this, "condition"));
	}

	public BooleanFormula evaluate() {
		return getCondition().accept(
			new BooleanReplacer(new HashMap<BooleanVariable, BooleanFormula>()) {
				@Override
				public BooleanFormula visit(BooleanVariable node) {
					switch(((Variable)node).getState())
					{
					case TRUE:
						return One.instance();
					case FALSE:
						return Zero.instance();
					default:
						return node;
					}
				}
			}
		);
	}

	@Override
	public <T> T accept(CpogVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
