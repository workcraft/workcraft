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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;

import org.apache.batik.ext.awt.geom.Polygon2D;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.plugins.circuit.Contact.IOType;


@DisplayName("Input/output port")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/circuit-port.svg")

public class VisualContact extends VisualComponent implements StateObserver {
	public enum Direction {	NORTH, SOUTH, EAST, WEST};
	public static final Color inputColor = Color.RED;
	public static final Color outputColor = Color.BLUE;

	private static Font nameFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);

	private GlyphVector nameGlyph = null;

	private Direction direction = Direction.WEST;

	private Shape shape=null;
	double strokeWidth = 0.05;


	public VisualContact(Contact contact) {
		super(contact);

		contact.addObserver(this);
		addPropertyDeclarations();
	}

	public VisualContact(Contact component, VisualContact.Direction dir, String label) {
		super(component);

		component.addObserver(this);
		addPropertyDeclarations();

		setName(label);
		setDirection(dir);
	}

	private Shape getShape() {

		if (shape!=null) {
			return shape;
		}

		double size = getSize();
		if (getParent() instanceof VisualCircuitComponent) {

			shape = new Rectangle2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth
				);

		} else {
			float xx[] = {	(float) -(size / 2),
							(float) (size / 2),
							(float) size,
							(float) (size / 2),
							(float) -(size / 2)};
			float yy[] = {	(float) -(size / 2),
							(float) -(size / 2), 0.0f,
							(float) (size / 2),
							(float) (size / 2)};

			Polygon2D poly = new Polygon2D(xx, yy, 5);
			shape = poly;
		}

		return shape;
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

		if (!(getParent() instanceof VisualCircuitComponent)) {
			AffineTransform at = new AffineTransform();

			switch (getDirection()) {
			case NORTH:
				at.quadrantRotate(-1);
				break;
			case SOUTH:
				at.quadrantRotate(1);
				break;
			case EAST:
				at.quadrantRotate(2);
				break;
			}

			g.transform(at);

		}

		g.setColor(Coloriser.colorise(getFillColor(), getColorisation()));
		g.fill(getShape());
		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));

		g.setStroke(new BasicStroke((float)strokeWidth));
		g.draw(getShape());

		if (!(getParent() instanceof VisualCircuitComponent)) {
			AffineTransform at = new AffineTransform();

			switch (getDirection()) {
			case SOUTH:
				at.quadrantRotate(2);
				break;
			case EAST:
				at.quadrantRotate(2);
				break;
			}

			g.transform(at);

			GlyphVector gv = getNameGlyphs(g);
			Rectangle2D cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((getIOType()==IOType.INPUT)?inputColor:outputColor, getColorisation()));
			g.drawGlyphVector(gv, (float)(-cur.getWidth()/2), -0.5f);

		}
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

		Point2D p2 = new Point2D.Double();
		p2.setLocation(pointInLocalSpace);

		if (!(getParent() instanceof VisualCircuitComponent)) {
			AffineTransform at = new AffineTransform();

			switch (getDirection()) {
			case NORTH:
				at.quadrantRotate(1);
				break;
			case SOUTH:
				at.quadrantRotate(-1);
				break;
			case EAST:
				at.quadrantRotate(2);
				break;
			}

			at.transform(pointInLocalSpace, p2);
		}

		return getShape().contains(p2);
	}


/*	@Override
 * 	public Collection<MathNode> getMathReferences() {
		return Collections.emptyList();
	}

	*/

	/////////////////////////////////////////////////////////
	public GlyphVector getNameGlyphs(Graphics2D g) {
		if (nameGlyph == null) {
			if (getDirection()==VisualContact.Direction.NORTH||getDirection()==VisualContact.Direction.SOUTH) {
				AffineTransform at = new AffineTransform();
				at.quadrantRotate(1);
			}
			nameGlyph = nameFont.createGlyphVector(g.getFontRenderContext(), getName());
		}

		return nameGlyph;
	}

	public Rectangle2D getNameBB(Graphics2D g) {
		return getNameGlyphs(g).getVisualBounds();
	}

	public void setDirection(VisualContact.Direction dir) {

		if (dir==direction) return;

		this.direction=dir;

		nameGlyph = null;

		sendNotification(new PropertyChangedEvent(this, "direction"));
		sendNotification(new TransformChangedEvent(this));
	}

	public VisualContact.Direction getDirection() {
		return direction;
	}

	public void setIOType(Contact.IOType type) {
		((Contact)getReferencedComponent()).setIOType(type);
		sendNotification(new PropertyChangedEvent(this, "IOtype"));
	}

	public Contact.IOType getIOType() {
		return ((Contact)getReferencedComponent()).getIOType();
	}


	public String getName() {
		return ((Contact)getReferencedComponent()).getName();
	}

	public void setName(String name) {
/*		if (name==null||name.equals("")&&((Contact)getReferencedComponent()).getIOType()==IOType.INPUT)
			name=getNewName(
					((Contact)getReferencedComponent()).getParent(),
					"input");
		if (name==null||name.equals("")&&((Contact)getReferencedComponent()).getIOType()==IOType.OUTPUT)
			name=getNewName(
				((Contact)getReferencedComponent()).getParent(),
					"output");*/

		((Contact)getReferencedComponent()).setName(name);

		sendNotification(new PropertyChangedEvent(this, "name"));
	}

	@Override
	public void notify(StateEvent e) {
		nameGlyph = null;
	}


}
