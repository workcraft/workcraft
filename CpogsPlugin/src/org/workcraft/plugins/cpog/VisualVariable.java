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
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_X)
@SVGIcon("images/icons/svg/variable.svg")
public class VisualVariable extends VisualComponent
{
	private static double size = 1;
	private static float strokeWidth = 0.08f;

	private static Font valueFont;

	private static Font labelFont;

	static {
		try {
			labelFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
			valueFont = labelFont.deriveFont(0.75f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Rectangle2D labelBB = null;
	public LabelPositioning labelPositioning = LabelPositioning.TOP;

	public VisualVariable(Variable variable)
	{
		super(variable);

		LinkedHashMap<String, Object> states = new LinkedHashMap<String, Object>();
		states.put("[1] true", VariableState.TRUE);
		states.put("[0] false", VariableState.FALSE);
		states.put("[?] undefined", VariableState.UNDEFINED);

		PropertyDescriptor declaration = new PropertyDeclaration(this, "State", "getState", "setState", VariableState.class, states);
		addPropertyDeclaration(declaration);

		LinkedHashMap<String, Object> positions = new LinkedHashMap<String, Object>();

		for(LabelPositioning lp : LabelPositioning.values())
			positions.put(lp.name, lp);

		declaration = new PropertyDeclaration(this, "Label positioning", "getLabelPositioning", "setLabelPositioning", LabelPositioning.class, positions);
		addPropertyDeclaration(declaration);
	}

	public void draw(DrawRequest r)
	{
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();

		Shape shape = new Rectangle2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2,
				size - strokeWidth, size - strokeWidth);

		g.setStroke(new BasicStroke(strokeWidth));

		g.setColor(Coloriser.colorise(getFillColor(), colorisation));
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

		String text = getLabel();

		FormulaRenderingResult result = FormulaToGraphics.print(text, labelFont, g.getFontRenderContext());

		labelBB = result.boundingBox;
		Rectangle2D bb = getBoundingBoxInLocalSpace();
		Point2D labelPosition = new Point2D.Double(
				bb.getCenterX() - labelBB.getCenterX() + 0.5 * labelPositioning.dx * (bb.getWidth() + labelBB.getWidth() + 0.2),
				bb.getCenterY() - labelBB.getCenterY() + 0.5 * labelPositioning.dy * (bb.getHeight() + labelBB.getHeight() + 0.2));

		AffineTransform oldTransform = g.getTransform();
		AffineTransform transform = AffineTransform.getTranslateInstance(labelPosition.getX(), labelPosition.getY());

		g.translate(labelPosition.getX(), labelPosition.getY());
		result.draw(g, Coloriser.colorise(getLabelColor(), r.getDecoration().getColorisation()));
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
