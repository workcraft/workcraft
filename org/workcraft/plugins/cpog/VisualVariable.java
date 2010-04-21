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
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_X)
@SVGIcon("images/icons/svg/variable.svg")
public class VisualVariable extends VisualComponent
{
	private static Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	private static double size = 1;
	private static float strokeWidth = 0.1f;

	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);

	private Rectangle2D labelBB = null;

	public VisualVariable(Variable variable)
	{
		super(variable);

		LinkedHashMap<String, Object> states = new LinkedHashMap<String, Object>();
		states.put("[1] true", VariableState.TRUE);
		states.put("[0] false", VariableState.FALSE);
		states.put("[?] undefined", VariableState.UNDEFINED);

		PropertyDescriptor declaration = new PropertyDeclaration(this, "State", "getState", "setState", VariableState.class, states);
		addPropertyDeclaration(declaration);
	}

	public void draw(Graphics2D g)
	{

		Shape shape = new Rectangle2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2,
				size - strokeWidth, size - strokeWidth);

		g.setStroke(new BasicStroke(strokeWidth));

		g.setColor(Coloriser.colorise(getFillColor(), getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.draw(shape);

		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.setFont(font);

		String text = getState().toString();

		GlyphVector glyphVector = font.createGlyphVector(g.getFontRenderContext(), text);
		Rectangle2D textBB = glyphVector.getLogicalBounds();

		float textX = (float)-textBB.getCenterX();
		float textY = (float)-textBB.getCenterY();

		g.drawGlyphVector(glyphVector, textX, textY);
		drawLabelInLocalSpace(g);
	}

	@NoAutoSerialisation
	@Override
	public String getLabel()
	{
		return getMathVariable().getLabel();
	}

	@NoAutoSerialisation
	@Override
	public void setLabel(String label)
	{
		getMathVariable().setLabel(label);
		sendNotification(new PropertyChangedEvent(this, "label"));
	}

	protected void drawLabelInLocalSpace(Graphics2D g)
	{
		String text = getLabel();

		final GlyphVector glyphs = labelFont.createGlyphVector(g.getFontRenderContext(), text);

		labelBB = glyphs.getLogicalBounds();
		Rectangle2D bb = getBoundingBoxInLocalSpace();
		Point2D labelPosition = new Point2D.Double(bb.getMinX() + (bb.getWidth() - labelBB.getWidth()) * 0.5,
				bb.getMinY() - 0.2);

		labelBB = glyphs.getVisualBounds();
		labelBB.setRect(labelBB.getMinX() + labelPosition.getX(), labelBB.getMinY() + labelPosition.getY(),
				labelBB.getWidth(), labelBB.getHeight());

		g.setColor(Coloriser.colorise(getLabelColor(), getColorisation()));
		g.drawGlyphVector(glyphs, (float) labelPosition.getX(), (float) labelPosition.getY());
	}

	public Rectangle2D getBoundingBoxInLocalSpace()
	{
		double size = CommonVisualSettings.getSize();
		return new Rectangle2D.Double(-size / 2, -size / 2, size, size);
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

	public Variable getMathVariable()
	{
		return (Variable)getReferencedComponent();
	}

	public VariableState getState()
	{
		return getMathVariable().getState();
	}

	public void setState(VariableState state)
	{
		getMathVariable().setState(state);
	}

	public Rectangle2D getBoundingBoxWithLabel()
	{
		return transformToParentSpace(BoundingBoxHelper.union(getBoundingBoxInLocalSpace(), labelBB));
	}

	public void toggle()
	{
		setState(getState().toggle());
	}
}
