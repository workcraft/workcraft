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

package org.workcraft.plugins.xmas.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.XmasContact.IOType;


public class VisualXmasContact extends VisualComponent implements StateObserver {
	public static final String IO_TYPE_PROPERTY_NAME = "IOtype";

	private double size = 0.3;

	public VisualXmasContact(XmasContact contact) {
		super(contact);

		contact.addObserver(this);
		addPropertyDeclarations();
	}

	public VisualXmasContact(XmasContact component, String label) {
		super(component);
		component.addObserver(this);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualXmasContact, IOType>(
				this, XmasContact.PROPERTY_IO_TYPE, IOType.class, false, false, false) {
			protected void setter(VisualXmasContact object, IOType value) {
				object.setIOType(value);
			}
			protected IOType getter(VisualXmasContact object) {
				return object.getIOType();
			}
		});
	}

	public void setIOType(XmasContact.IOType type) {
		getReferencedContact().setIOType(type);
		sendNotification(new PropertyChangedEvent(this, IO_TYPE_PROPERTY_NAME));
	}

	public XmasContact.IOType getIOType() {
		return getReferencedContact().getIOType();
	}

	public XmasContact getReferencedContact() {
		return (XmasContact)getReferencedComponent();
	}

	private Shape getShape() {
		if (getIOType() == IOType.INPUT) {
			return new Rectangle2D.Double(-0.5 * size, -0.5 * size, size, size);
		} else {
			return new Ellipse2D.Double(-0.5 * size, -0.5 * size, size, size);
		}
	}

	@Override
	public void draw(DrawRequest r) {

		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();

		boolean inSimulationMode = ((d.getColorisation() != null) || (d.getBackground() != null));
		if (inSimulationMode || XmasSettings.getShowContacts()) {
			Shape shape = getShape();
			g.setStroke(new BasicStroke((float)XmasSettings.getWireWidth()));

			Color fillColor = d.getBackground();
			if (fillColor == null) {
				fillColor = getFillColor();
			}
			g.setColor(fillColor);
			g.fill(shape);

			Color colorisation = d.getColorisation();
			g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
			g.draw(shape);
		}
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return getShape().getBounds2D();
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		Point2D p2 = new Point2D.Double();
		p2.setLocation(pointInLocalSpace);
		Shape shape = getShape();
		return shape.contains(p2);
	}

	public boolean isInput() {
		return getReferencedContact().isInput();
	}

	public boolean isOutput() {
		return getReferencedContact().isOutput();
	}

	@Override
	public void notify(StateEvent e) {
	}

}
