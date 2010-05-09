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
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.shared.CommonVisualSettings;

//@VisualClass("org.workcraft.plugins.circuit.VisualCircuitComponent")
@DisplayName("C.Component")

public class VisualCircuitComponent extends VisualComponent {

	private LinkedList<VisualContact> east = new LinkedList<VisualContact>();
	private LinkedList<VisualContact> west = new LinkedList<VisualContact>();
	private LinkedList<VisualContact> north = new LinkedList<VisualContact>();
	private LinkedList<VisualContact> south = new LinkedList<VisualContact>();

	private Color inputColor = Color.RED;
	private Color outputColor = Color.BLUE;

	double marginSize = 0.2;
	double contactLength = 1;

	double contactStep = 1;

//	private HashSet<VisualContact> inputs = new HashSet<VisualContact>();
//	private HashSet<VisualContact> outputs = new HashSet<VisualContact>();
	private HashSet<VisualContact> contacts = new HashSet<VisualContact> ();

	private Rectangle2D contactLabelBB = null;

/*	public HashSet<VisualContact> getContacts2() {
		HashSet<VisualContact> ret = new HashSet<VisualContact>();
		for (VisualComponent c: getContacts()) {
			if (c instanceof VisualContact) ret.add((VisualContact)c);
		}
		return ret;
	}
*/
	public VisualCircuitComponent(CircuitComponent component) {
//		super(component);
		// testing...

		addContact(new VisualContact(component.addInput(), VisualContact.Direction.west,"Req_in"));
		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.west, "Ack_out"));
		addContact(new VisualContact(component.addInput(), VisualContact.Direction.west, "Data_in"));
		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.east, "Req_out"));
		addContact(new VisualContact(component.addInput(), VisualContact.Direction.east, "Ack_in"));
		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.north, "Data_out"));
		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.north, "Data out 2"));
		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.south, "Reset"));

	}

	// updates sequential position of the
	private void updateStepPosition(LinkedList<VisualContact> side) {
		double step_pos=-contactStep*(side.size()-1)/2;

		for (VisualContact vc: side) {
			if (vc.getDirection()==VisualContact.Direction.east||
				vc.getDirection()==VisualContact.Direction.west) {

				vc.setY(step_pos);
			} else {
				vc.setX(step_pos);
			}
			step_pos += contactStep;
		}
	}

	private void updateSidePosition(Rectangle2D labelBB) {

		double side_pos_w = (double)(Math.round((labelBB.getMinX()-contactLength)*4))/4;
		double side_pos_e = (double)(Math.round((labelBB.getMaxX()+contactLength)*4))/4;
		double side_pos_s = (double)(Math.round((labelBB.getMaxY()+contactLength)*4))/4;
		double side_pos_n = (double)(Math.round((labelBB.getMinY()-contactLength)*4))/4;

		for (VisualContact vc: contacts) {
			switch (vc.getDirection()) {
				case east:
					vc.setX(side_pos_e);
					break;
				case west:
					vc.setX(side_pos_w);
					break;
				case north:
					vc.setY(side_pos_n);
					break;
				case south:
					vc.setY(side_pos_s);
					break;
			}
		}
	}

	public void updateDirection(VisualContact vc, VisualContact.Direction dir) {
		if (contacts.contains(vc)) {
			east.remove(vc);
			west.remove(vc);
			north.remove(vc);
			south.remove(vc);
			switch (dir) {
				case north: north.add(vc); updateStepPosition(north); break;
				case south: south.add(vc); updateStepPosition(south); break;
				case east: east.add(vc); updateStepPosition(east); break;
				case west: west.add(vc); updateStepPosition(west); break;
			}
		}
	}

	public void addContact(VisualContact vc) {
		if (!contacts.contains(vc)) {
			contacts.add(vc);
			switch (vc.getDirection()) {
				case north: north.add(vc); updateStepPosition(north); break;
				case south: south.add(vc); updateStepPosition(south); break;
				case east: east.add(vc); updateStepPosition(east); break;
				case west: west.add(vc); updateStepPosition(west); break;
			}
		}
	}

	protected Rectangle2D getContactLabelBB(Graphics2D g) {
		if (contactLabelBB==null) {
			Rectangle2D cur;
			double xx;
			double width_w=0;
			double width_e=0;
			double width_n=0;
			double width_s=0;

			for (VisualContact c: contacts) {
				GlyphVector gv = c.getNameGlyphs(g);
				cur = gv.getVisualBounds();
				xx = cur.getWidth();
				xx = (double)(Math.round(xx*4))/4;


				switch (c.getDirection()) {
					case west:
						width_w=(xx>width_w)?xx:width_w;
						break;
					case east:
						width_e=(xx>width_e)?xx:width_e;
						break;
					case north:
						width_n=(xx>width_n)?xx:width_n;
						break;
					case south:
						width_s=(xx>width_s)?xx:width_s;
						break;
				}
			}

			double height = Math.max(east.size(), west.size())*contactStep+width_n+width_s+marginSize*4;
			double width = Math.max(north.size(), south.size())*contactStep+width_e+width_w+marginSize*4;

			contactLabelBB = new Rectangle2D.Double(-width/2, -height/2, width, height);
			updateSidePosition(contactLabelBB);
		}
		return contactLabelBB;
	}

	protected void drawContactConnectionsInLocalSpace(Graphics2D g) {
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));

		Rectangle2D BB = getContactLabelBB(g);

		for (VisualContact c: west) {
			Line2D line = new Line2D.Double(c.getX(), c.getY(), BB.getMinX(), c.getY());
			g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), getColorisation()));
			g.draw(line);
		}

		for (VisualContact c: east) {
			Line2D line = new Line2D.Double(c.getX(), c.getY(), BB.getMaxX(), c.getY());
			g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), getColorisation()));
			g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
			g.draw(line);
		}

		for (VisualContact c: north) {
			Line2D line = new Line2D.Double(c.getX(), c.getY(), c.getX(), BB.getMinY());
			g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), getColorisation()));
			g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
			g.draw(line);

		}

		for (VisualContact c: south) {
			Line2D line = new Line2D.Double(c.getX(), c.getY(), c.getX(), BB.getMaxY());
			g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), getColorisation()));
			g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
			g.draw(line);
		}
	}


	protected void drawContactsInLocalSpace(Graphics2D g) {

		Rectangle2D cur;

		Rectangle2D BB = getContactLabelBB(g);

		double step_pos;

		AffineTransform tt = g.getTransform();


		for (VisualContact c: west) {
			GlyphVector gv = c.getNameGlyphs(g);
			cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((c.getIOType()==IOType.input)?inputColor:outputColor, getColorisation()));
			step_pos = c.getY();

			g.drawGlyphVector(gv, (float)(BB.getMinX()+marginSize), (float)(step_pos+(cur.getHeight())/2));
		}

		for (VisualContact c: east) {

			GlyphVector gv = c.getNameGlyphs(g);
			cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((c.getIOType()==IOType.input)?inputColor:outputColor, getColorisation()));

			step_pos = c.getY();
			g.drawGlyphVector(gv, (float)(BB.getMaxX()-marginSize-cur.getWidth()), (float)(step_pos+(cur.getHeight())/2));
		}

		AffineTransform at = new AffineTransform();
		at.quadrantRotate(-1);
		//
		g.transform(at);

		for (VisualContact c: north) {

			GlyphVector gv = c.getNameGlyphs(g);
			cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((c.getIOType()==IOType.input)?inputColor:outputColor, getColorisation()));

			step_pos = c.getX();
			g.drawGlyphVector(gv, (float)(BB.getMaxY()-marginSize-cur.getWidth()), (float)(step_pos+(cur.getHeight())/2));
		}

		for (VisualContact c: south) {

			GlyphVector gv = c.getNameGlyphs(g);
			cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((c.getIOType()==IOType.input)?inputColor:outputColor, getColorisation()));

			step_pos = c.getX();
			g.drawGlyphVector(gv, (float)(BB.getMinY()+marginSize), (float)(step_pos+(cur.getHeight())/2));
		}

		for (VisualContact c: contacts) {

			g.setTransform(tt);
			c.draw(g);
		}
		g.setTransform(tt);
	}

	@Override
	public void draw(Graphics2D g) {

		drawContactConnectionsInLocalSpace(g);

		Rectangle2D shape = getContactLabelBB(g);

		g.setColor(Coloriser.colorise(CommonVisualSettings.getFillColor(), getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), getColorisation()));
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		g.draw(shape);

		drawContactsInLocalSpace(g);
//		drawLabelInLocalSpace(g);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		double size = CommonVisualSettings.getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}


	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

}
