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

package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.circuit.Contact.IOType;

public class VisualContact extends VisualComponent {
	public enum Direction {north, south, east, west};

	private static Font nameFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	private GlyphVector nameGlyphs = null;
	private Direction direction = Direction.west;

	VisualCircuitComponent parentComponent;

	public VisualCircuitComponent getParentConnection() {
//		return parentConnection;
		return null;
	}

	@Override
	public void draw(Graphics2D g) {
		double size = getSize();
		double strokeWidth = 0.05;


		Shape shape = new Rectangle2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);

		g.setColor(Coloriser.colorise(getFillColor(), getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.setStroke(new BasicStroke((float)strokeWidth));
		g.draw(shape);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		double size = getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}

	private double getSize() {
		return 0.5;
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

	@Override
	public Collection<MathNode> getMathReferences() {
		//TODO
		return Collections.emptyList();
	}

	/////////////////////////////////////////////////////////
	public GlyphVector getNameGlyphs(Graphics2D g) {
		if (nameGlyphs == null) {
			if (getDirection()==Direction.north||getDirection()==Direction.south) {
				AffineTransform at = new AffineTransform();
				at.quadrantRotate(1);
			}
			nameGlyphs = nameFont.createGlyphVector(g.getFontRenderContext(), getContactName());
		}

		return nameGlyphs;
	}

	public Rectangle2D getNameBB(Graphics2D g) {
		return getNameGlyphs(g).getVisualBounds();
	}

	public String getContactName() {
//		if (getReferencedComponent()!=null) return getReferencedComponent().getLabel();
		return getLabel();
	}

	public void setContactName(String name) {
//		nameGlyphs = null;
		setLabel(name);
//		if (getReferencedComponent()!=null) getReferencedComponent().setLabel(name);
	}

	public void setDirection(Direction dir) {
		if (parentComponent!=null) {
			parentComponent.updateDirection(this, dir);
		}
		this.direction=dir;
		nameGlyphs = null;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setIOType(IOType type) {
		((Contact)getReferencedComponent()).setIOType(type);
	}

	public IOType getIOType() {
		return ((Contact)getReferencedComponent()).getIOType();
	}

	public VisualContact(Contact component, Direction dir, String label) {
		super(component);
		setLabel(label);
		if (dir==null) {
			if (component!=null) {
				if (component.getIOType()==IOType.output) {
					setDirection(Direction.east);
				}
			}
		} else {
			direction = dir;
		}
		parentComponent = null;

	}

}
