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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.Colorisable;
import org.workcraft.dom.visual.CustomTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Hierarchy;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

//@VisualClass("org.workcraft.plugins.circuit.VisualCircuitComponent")
@DisplayName("Abstract Component")

public class VisualCircuitComponent extends VisualComponent implements Container, CustomTouchable, StateObserver {

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
	private Rectangle2D totalBB = null;

/*	public HashSet<VisualContact> getContacts2() {
		HashSet<VisualContact> ret = new HashSet<VisualContact>();
		for (VisualComponent c: getContacts()) {
			if (c instanceof VisualContact) ret.add((VisualContact)c);
		}
		return ret;
	}
*/
	public VisualCircuitComponent(CircuitComponent component) {
		super(component);
		// testing...

		//addContact(new VisualContact(component.addInput(), VisualContact.Direction.WEST,"Req_in"));
		addInput("Req_in");
		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.WEST, "Ack_out"));
		addContact(new VisualContact(component.addInput(), VisualContact.Direction.WEST, "Data_in"));
		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.EAST, "Req_out"));
		addContact(new VisualContact(component.addInput(), VisualContact.Direction.EAST, "Ack_in"));

		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.NORTH, "Data_out"));
		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.NORTH, "Data out 2"));
		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.SOUTH, "Reset"));

	}

	// updates sequential position of the contacts
	private void updateStepPosition(LinkedList<VisualContact> side) {
		double step_pos=-contactStep*(side.size()-1)/2;

		for (VisualContact vc: side) {
			if (vc.getDirection()==VisualContact.Direction.EAST||
				vc.getDirection()==VisualContact.Direction.WEST) {

				vc.setY(step_pos);
			} else {
				vc.setX(step_pos);
			}
			step_pos += contactStep;
		}
	}

	private void updateSidePosition(Rectangle2D labelBB) {

		double side_pos_w = (double)(Math.round((labelBB.getMinX()-contactLength)*2))/2;
		double side_pos_e = (double)(Math.round((labelBB.getMaxX()+contactLength)*2))/2;
		double side_pos_s = (double)(Math.round((labelBB.getMaxY()+contactLength)*2))/2;
		double side_pos_n = (double)(Math.round((labelBB.getMinY()-contactLength)*2))/2;

		for (VisualContact vc: contacts) {
			switch (vc.getDirection()) {
				case EAST:
					vc.setX(side_pos_e);
					break;
				case WEST:
					vc.setX(side_pos_w);
					break;
				case NORTH:
					vc.setY(side_pos_n);
					break;
				case SOUTH:
					vc.setY(side_pos_s);
					break;
			}
		}
	}

	public boolean reassignDirection(VisualContact vc, VisualContact.Direction dir) {
		if (dir==Direction.NORTH&&north.contains(vc)) return false;
		if (dir==Direction.SOUTH&&south.contains(vc)) return false;
		if (dir==Direction.EAST&&east.contains(vc)) return false;
		if (dir==Direction.WEST&&west.contains(vc)) return false;

		if (contacts.contains(vc)) {
			east.remove(vc);
			west.remove(vc);
			north.remove(vc);
			south.remove(vc);
			switch (dir) {
				case NORTH: north.add(vc); return true;
				case SOUTH: south.add(vc); return true;
				case EAST: east.add(vc); return true;
				case WEST: west.add(vc); return true;
			}
		}

		return false;
	}

	public void updateDirection(VisualContact vc, VisualContact.Direction dir) {
		contactLabelBB = null;

		if (reassignDirection(vc, dir)) {
			switch (dir) {
				case NORTH: updateStepPosition(north); break;
				case SOUTH: updateStepPosition(south); break;
				case EAST: updateStepPosition(east); break;
				case WEST: updateStepPosition(west); break;
			}
		}
	}

	public void addContact(VisualContact vc) {
		if (!contacts.contains(vc)) {
			contacts.add(vc);
			vc.setParent(this);

			switch (vc.getDirection()) {
				case NORTH: north.add(vc); updateStepPosition(north); break;
				case SOUTH: south.add(vc); updateStepPosition(south); break;
				case EAST: east.add(vc); updateStepPosition(east); break;
				case WEST: west.add(vc); updateStepPosition(west); break;
			}

			vc.addObserver(this);
			contactLabelBB = null;
		}
	}


	protected void updateTotalBB() {

		totalBB = BoundingBoxHelper.mergeBoundingBoxes(Hierarchy.getChildrenOfType(this, Touchable.class));

		if (contactLabelBB!=null)
			Rectangle2D.union(totalBB, contactLabelBB, totalBB);
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
					case WEST:
						width_w=(xx>width_w)?xx:width_w;
						break;
					case EAST:
						width_e=(xx>width_e)?xx:width_e;
						break;
					case NORTH:
						width_n=(xx>width_n)?xx:width_n;
						break;
					case SOUTH:
						width_s=(xx>width_s)?xx:width_s;
						break;
				}
			}

			double height = Math.max(east.size(), west.size())*contactStep+width_n+width_s+marginSize*4;
			double width = Math.max(north.size(), south.size())*contactStep+width_e+width_w+marginSize*4;

			contactLabelBB = new Rectangle2D.Double(-width/2, -height/2, width, height);
			updateSidePosition(contactLabelBB);
			updateTotalBB();
		}
		return contactLabelBB;
	}

	protected void drawContactConnections(Graphics2D g) {
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


	protected void drawContacts(Graphics2D g) {

		Rectangle2D cur;

		Rectangle2D BB = getContactLabelBB(g);

		double step_pos;

		for (VisualContact c: west) {
			GlyphVector gv = c.getNameGlyphs(g);
			cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, getColorisation()));
			step_pos = c.getY();

			g.drawGlyphVector(gv, (float)(BB.getMinX()+marginSize), (float)(step_pos+(cur.getHeight())/2));
		}

		for (VisualContact c: east) {

			GlyphVector gv = c.getNameGlyphs(g);
			cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, getColorisation()));

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
			g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, getColorisation()));

			step_pos = c.getX();
			g.drawGlyphVector(gv, (float)(BB.getMaxY()-marginSize-cur.getWidth()), (float)(step_pos+(cur.getHeight())/2));
		}

		for (VisualContact c: south) {

			GlyphVector gv = c.getNameGlyphs(g);
			cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, getColorisation()));

			step_pos = c.getX();
			g.drawGlyphVector(gv, (float)(BB.getMinY()+marginSize), (float)(step_pos+(cur.getHeight())/2));
		}
	}

	@Override
	public void draw(Graphics2D g) {

		drawContactConnections(g);

		Rectangle2D shape = getContactLabelBB(g);

		g.setColor(Coloriser.colorise(CommonVisualSettings.getFillColor(), getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), getColorisation()));
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		g.draw(shape);

		drawContacts(g);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
//		if (contactLabelBB!=null) return contactLabelBB;
		if (totalBB!=null) return totalBB;

		double size = CommonVisualSettings.getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}


	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (contactLabelBB!=null) return contactLabelBB.contains(pointInLocalSpace);
		return false;
	}

	@Override
	public Collection<Node> getChildren() {
		return Collections.<Node>unmodifiableCollection(contacts);
	}

	@Override
	public void add(Node node) {
		throw new NotImplementedException();
	}

	@Override
	public void add(Collection<Node> nodes) {
		throw new NotImplementedException();
	}

	@Override
	public void remove(Node node) {
		throw new NotImplementedException();
	}

	@Override
	public void remove(Collection<Node> node) {
		throw new NotImplementedException();
	}

	@Override
	public void reparent(Collection<Node> nodes) {
		throw new NotImplementedException();
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		throw new NotImplementedException();
	}

	public void setColorisation(Color color) {
		super.setColorisation(color);
		for (Colorisable node : Hierarchy.getChildrenOfType(this, Colorisable.class))
			node.setColorisation(color);
	}

	@Override
	public Node customHitTest(Point2D point) {
		Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
		for(VisualContact contact : contacts)
			if(contact.hitTest(pointInLocalSpace))
				return contact;
		if(hitTest(point))
			return this;
		else
			return null;
	}

	@Override
	public void notify(StateEvent e) {
		if (e instanceof TransformChangedEvent) {


			TransformChangedEvent t = (TransformChangedEvent)e;

			VisualContact vc = (VisualContact)t.sender;

			AffineTransform at = t.sender.getTransform();
			double x = at.getTranslateX();
			double y = at.getTranslateY();

			Rectangle2D r = contactLabelBB;
			if (r==null) return;

			updateTotalBB();

			VisualContact.Direction dir = vc.getDirection();

			if (x<r.getMinX()&&y>r.getMinY()&&y<r.getMaxY()) dir = Direction.WEST;
			if (x>r.getMaxX()&&y>r.getMinY()&&y<r.getMaxY()) dir = Direction.EAST;
			if (y<r.getMinY()&&x>r.getMinX()&&x<r.getMaxX()) dir = Direction.NORTH;
			if (y>r.getMaxY()&&x>r.getMinX()&&x<r.getMaxX()) dir = Direction.SOUTH;

 			vc.setDirection(dir);

/*			this.direction=dir;
			if (getParent()!=null) {
				((VisualCircuitComponent)getParent()).reassignDirection(this, dir);
			}*/

		}

	}

	public String getNewName(String start) {
		// iterate through all contacts, check that the name doesn't exist
		int num=0;
		boolean found = true;
		while (found) {
			num++;
			found=false;
			for (VisualContact c : contacts) {
				if (c.getName().equals(start+num)) {
					found=true;
				}
			}
		}
		return start+num;
	}

	public void addInput(String name) {
		if (name.equals("")) name = getNewName("input");
		CircuitComponent component = (CircuitComponent)getReferencedComponent();
		addContact(new VisualContact(component.addInput(), VisualContact.Direction.WEST, name));
	}

	public void addOutput(String name) {
		if (name.equals("")) name = getNewName("output");
		CircuitComponent component = (CircuitComponent)getReferencedComponent();
		addContact(new VisualContact(component.addOutput(), VisualContact.Direction.EAST, name));
	}

}
