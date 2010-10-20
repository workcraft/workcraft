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
import java.util.LinkedHashMap;

import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.plugins.shared.CommonVisualSettings;

@Hotkey(KeyEvent.VK_V)
@SVGIcon("images/icons/svg/vertex.svg")
public class VisualVertex extends VisualComponent
{
	private final static double size = 1;
	private final static float strokeWidth = 0.1f;
	private Rectangle2D labelBB = null;
	public LabelPositioning labelPositioning = LabelPositioning.TOP;

	private static Font labelFont;

	static {
		try {
			labelFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public VisualVertex(Vertex vertex)
	{
		super(vertex);

		LinkedHashMap<String, Object> positions = new LinkedHashMap<String, Object>();

		for(LabelPositioning lp : LabelPositioning.values())
			positions.put(lp.name, lp);

		PropertyDescriptor declaration = new PropertyDeclaration(this, "Label positioning", "getLabelPositioning", "setLabelPositioning", LabelPositioning.class, positions);
		addPropertyDeclaration(declaration);
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


		labelBB = result.boundingBox;
/*		Rectangle2D bb = getBoundingBoxInLocalSpace();
		Point2D labelPosition = new Point2D.Double(
				bb.getCenterX() - labelBB.getCenterX() + 0.5 * labelPositioning.dx * (bb.getWidth() + labelBB.getWidth() + 0.2),
				bb.getCenterY() - labelBB.getCenterY() + 0.5 * labelPositioning.dy * (bb.getHeight() + labelBB.getHeight() + 0.2));
*/
		Point2D labelPosition = new Point2D.Double(
				-labelBB.getCenterX() + 0.5 * labelPositioning.dx * (1.0 + labelBB.getWidth() + 0.2),
				-labelBB.getCenterY() + 0.5 * labelPositioning.dy * (1.0 + labelBB.getHeight() + 0.2));

		AffineTransform oldTransform = g.getTransform();
		AffineTransform transform = AffineTransform.getTranslateInstance(labelPosition.getX(), labelPosition.getY());

		g.translate(labelPosition.getX(), labelPosition.getY());
		result.draw(g, Coloriser.colorise(getLabelColor(), colorisation));
		g.setTransform(oldTransform);

		labelBB = BoundingBoxHelper.transform(labelBB, transform);
	}

	public Rectangle2D getBoundingBoxInLocalSpace()
	{
		return BoundingBoxHelper.union(labelBB, new Rectangle2D.Double(-size / 2, -size / 2, size, size));
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
		if (labelBB != null && labelBB.contains(pointInLocalSpace)) return true;

		double size = CommonVisualSettings.getSize();

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

	public LabelPositioning getLabelPositioning()
	{
		return labelPositioning;
	}

	public void setLabelPositioning(LabelPositioning labelPositioning)
	{
		this.labelPositioning = labelPositioning;
		sendNotification(new PropertyChangedEvent(this, "label positioning"));
	}

	public BooleanFormula evaluate() {
		return getCondition().accept(
			new BooleanReplacer(new HashMap<BooleanVariable, BooleanFormula>())
			{
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
}
