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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_X)
@SVGIcon("images/icons/svg/variable.svg")
public class VisualVariable extends VisualComponent
{
	private final static double size = 1.0;
	private final static float strokeWidth = 0.08f;
	private static Font valueFont;
	private static Font labelFont;
	private Point2D labelPosition = null;
	private Rectangle2D labelBoundingBox = null;

	static {
		try {
			Font font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb"));
			labelFont = font.deriveFont(0.5f);
			valueFont = font.deriveFont(0.75f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public VisualVariable(Variable variable)
	{
		super(variable);

		addPropertyDeclaration(new PropertyDeclaration<VisualVariable, VariableState>(
				this, "State", VariableState.class, VariableState.getChoice()) {
			public void setter(VisualVariable object, VariableState value) {
				object.setState(value);
			}
			public VariableState getter(VisualVariable object) {
				return object.getState();
			}
		});
	}

	@Override
	public void draw(DrawRequest r)
	{
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		Color background = r.getDecoration().getBackground();

		Shape shape = new Rectangle2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2,
				size - strokeWidth, size - strokeWidth);

		g.setStroke(new BasicStroke(strokeWidth));
		g.setColor(Coloriser.colorise(getFillColor(), background));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
		g.draw(shape);

		String text = getState().toString();
		FormulaRenderingResult result = FormulaToGraphics.print(text, valueFont, g.getFontRenderContext());
		Rectangle2D textBB = result.boundingBox;

		float textX = (float)-textBB.getCenterX();
		float textY = (float)-textBB.getCenterY() + 0.08f;

		AffineTransform transform = g.getTransform();
		g.translate(textX, textY);
		result.draw(g, Coloriser.colorise(getForegroundColor(), colorisation));
		g.setTransform(transform);
		drawLabelInLocalSpace(r);
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

	protected void drawLabelInLocalSpace(DrawRequest r)
	{
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		Rectangle2D bb = new Rectangle2D.Double(-size / 2, -size / 2, size, size);

		FormulaRenderingResult result = FormulaToGraphics.print(getLabel(), labelFont, g.getFontRenderContext());
		Rectangle2D gbb = result.boundingBox;

		labelPosition = new Point2D.Double(
				bb.getCenterX() - gbb.getCenterX() + 0.5 * getLabelPositioning().xOffset * (bb.getWidth() + gbb.getWidth() + 0.2),
				bb.getCenterY() - gbb.getCenterY() + 0.5 * getLabelPositioning().yOffset * (bb.getHeight() + gbb.getHeight() + 0.2));
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
		if (labelBoundingBox != null && labelBoundingBox.contains(pointInLocalSpace)) return true;
		return Math.abs(pointInLocalSpace.getX()) <= size / 2 && Math.abs(pointInLocalSpace.getY()) <= size / 2;
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

	public void toggle()
	{
		setState(getState().toggle());
	}

}
