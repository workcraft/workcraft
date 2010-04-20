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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;

import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.shared.CommonVisualSettings;

@Hotkey(KeyEvent.VK_V)
@SVGIcon("images/icons/svg/vertex.svg")
public class VisualVertex extends VisualComponent
{
	private final static Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	private final static double size = 1;
	private final static float strokeWidth = 0.1f;
	private Rectangle2D labelBB = null;
	public LabelPositioning labelPositioning = LabelPositioning.TOP;

	public VisualVertex(Vertex vertex)
	{
		super(vertex);

		LinkedHashMap<String, Object> positions = new LinkedHashMap<String, Object>();

		for(LabelPositioning lp : LabelPositioning.values())
			positions.put(lp.name, lp);

		PropertyDescriptor declaration = new PropertyDeclaration(this, "Label positioning", "getLabelPositioning", "setLabelPositioning", LabelPositioning.class, positions);
		addPropertyDeclaration(declaration);
	}

	public void draw(Graphics2D g)
	{
		Shape shape = new Ellipse2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2,
				size - strokeWidth, size - strokeWidth);

		g.setStroke(new BasicStroke(strokeWidth));

		g.setColor(Coloriser.colorise(getFillColor(), getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.draw(shape);
		drawLabelInLocalSpace(g);
	}

	protected void drawLabelInLocalSpace(Graphics2D g)
	{
		String text = getLabel();
		String condition = FormulaToString.toString(getCondition(), true);
		if (getCondition() != One.instance()) text += ": " + condition;

		final GlyphVector glyphs = labelFont.createGlyphVector(g.getFontRenderContext(), text);

		labelBB = glyphs.getLogicalBounds();
		Rectangle2D bb = getBoundingBoxInLocalSpace();
		Point2D labelPosition = new Point2D.Double(
				bb.getCenterX() - labelBB.getCenterX() + 0.5 * labelPositioning.dx * (bb.getWidth() + labelBB.getWidth() + 0.2),
				bb.getCenterY() - labelBB.getCenterY() + 0.5 * labelPositioning.dy * (bb.getHeight() + labelBB.getHeight() + 0.2));

		labelBB = glyphs.getVisualBounds();
		labelBB.setRect(labelBB.getMinX() + labelPosition.getX(), labelBB.getMinY() + labelPosition.getY(),
				labelBB.getWidth(), labelBB.getHeight());

		g.setColor(Coloriser.colorise(getLabelColor(), getColorisation()));
		g.drawGlyphVector(glyphs, (float) labelPosition.getX(), (float) labelPosition.getY());
	}

	public Rectangle2D getBoundingBoxInLocalSpace()
	{
		return new Rectangle2D.Double(-size / 2, -size / 2, size, size);
	}

	public Rectangle2D getBoundingBoxWithLabel()
	{
		return transformToParentSpace(BoundingBoxHelper.union(getBoundingBoxInLocalSpace(), labelBB));
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
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
}
