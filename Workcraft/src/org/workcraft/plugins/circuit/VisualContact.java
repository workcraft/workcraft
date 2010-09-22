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
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("Input/output port")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/circuit-port.svg")

public class VisualContact extends VisualComponent {
	public enum Direction {	NORTH, SOUTH, EAST, WEST};

	private static Font nameFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);

	private String name = "";
	private GlyphVector nameGlyphs = null;
	private Direction direction = Direction.WEST;

	public VisualContact(Contact contact) {
		super(contact);
	}

	private void addPropertyDeclarations() {
		LinkedHashMap<String, Object> types = new LinkedHashMap<String, Object>();
		types.put("Input", Contact.IOType.INPUT);
		types.put("Output", Contact.IOType.OUTPUT);

		LinkedHashMap<String, Object> directions = new LinkedHashMap<String, Object>();
		directions.put("North", VisualContact.Direction.NORTH);
		directions.put("East", VisualContact.Direction.EAST);
		directions.put("South", VisualContact.Direction.SOUTH);
		directions.put("West", VisualContact.Direction.WEST);

		addPropertyDeclaration(new PropertyDeclaration(this, "Direction", "getDirection", "setDirection", VisualContact.Direction.class, directions));
		addPropertyDeclaration(new PropertyDeclaration(this, "I/O type", "getIOType", "setIOType", Contact.IOType.class, types));
		addPropertyDeclaration(new PropertyDeclaration(this, "Name", "getName", "setName", String.class));
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


/*	@Override
 * 	public Collection<MathNode> getMathReferences() {
		return Collections.emptyList();
	}

	*/

	/////////////////////////////////////////////////////////
	public GlyphVector getNameGlyphs(Graphics2D g) {
		if (nameGlyphs == null) {
			if (getDirection()==VisualContact.Direction.NORTH||getDirection()==VisualContact.Direction.SOUTH) {
				AffineTransform at = new AffineTransform();
				at.quadrantRotate(1);
			}
			nameGlyphs = nameFont.createGlyphVector(g.getFontRenderContext(), getName());
		}

		return nameGlyphs;
	}

	public Rectangle2D getNameBB(Graphics2D g) {
		return getNameGlyphs(g).getVisualBounds();
	}

	public void setDirection(VisualContact.Direction dir) {

		if (dir==direction) return;

		if (getParent()!=null) {
			((VisualCircuitComponent)getParent()).updateDirection(this, dir);
		}
		this.direction=dir;

		nameGlyphs = null;

		sendNotification(new PropertyChangedEvent(this, "direction"));
	}

	public VisualContact.Direction getDirection() {
		return direction;
	}

	public void setIOType(Contact.IOType type) {
		((Contact)getReferencedComponent()).setIOType(type);
	}

	public Contact.IOType getIOType() {
		return ((Contact)getReferencedComponent()).getIOType();
	}


	public String getName() {
		return name;
	}

	public void setName(String label) {
		this.name = label;
		nameGlyphs = null;

		sendNotification(new PropertyChangedEvent(this, "name"));
	}

	public VisualContact(Contact component, VisualContact.Direction dir, String label) {
		super(component);

		addPropertyDeclarations();

		setName(label);

		/*if (dir==null) {
			if (component!=null) {
				if (component.getIOType()==IOType.OUTPUT) {
					setDirection(VisualContact.Direction.EAST);
				} else {
					setDirection(VisualContact.Direction.WEST);
				}
			}
		} else {
			direction = dir;
		}*/
		direction = dir;
//		parentComponent = null;

	}


}
