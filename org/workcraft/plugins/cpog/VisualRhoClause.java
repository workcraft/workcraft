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
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.shared.CommonVisualSettings;

@Hotkey(KeyEvent.VK_R)
@SVGIcon("images/icons/svg/rho.svg")
public class VisualRhoClause extends VisualComponent
{
	private static float strokeWidth = 0.038f;

	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);

	private Rectangle2D boudingBox = new Rectangle2D.Float(0, 0, 0, 0);

	public VisualRhoClause(RhoClause rhoClause)
	{
		super(rhoClause);
		PropertyDescriptor declaration = new PropertyDeclaration(this, "Function", "getFunction", "setFunction", String.class);
		addPropertyDeclaration(declaration);
	}

	public void draw(Graphics2D g)
	{
		FontRenderContext fontRenderContext = g.getFontRenderContext();
		GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, getFunction());
		Rectangle2D textBB = glyphVector.getLogicalBounds();

		float textX = (float)-textBB.getCenterX();
		float textY = (float)-textBB.getCenterY();

		float width = (float)textBB.getWidth() + 0.4f;
		float height = (float)textBB.getHeight() + 0.2f;

		boudingBox = new Rectangle2D.Float(-width / 2, -height / 2, width, height);

		g.setStroke(new BasicStroke(strokeWidth));

		g.setColor(Coloriser.colorise(getFillColor(), getColorisation()));
		g.fill(boudingBox);
		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.draw(boudingBox);

		g.drawGlyphVector(glyphVector, textX, textY);
	}

	public Rectangle2D getBoundingBoxInLocalSpace()
	{
		return boudingBox;
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

	public RhoClause getMathRhoClause()
	{
		return (RhoClause)getReferencedComponent();
	}

	public String getFunction()
	{
		return getMathRhoClause().getFunction().value;
	}

	public void setFunction(String function)
	{
		getMathRhoClause().setFunction(new BooleanFunction(function));
	}
}
