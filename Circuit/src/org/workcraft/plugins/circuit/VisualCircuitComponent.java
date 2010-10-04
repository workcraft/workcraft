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
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.Colorisable;
import org.workcraft.dom.visual.CustomTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Hierarchy;

//@VisualClass("org.workcraft.plugins.circuit.VisualCircuitComponent")

@DisplayName("Abstract Component")
@Hotkey(KeyEvent.VK_A)
@SVGIcon("images/icons/svg/circuit-component.svg")

public class VisualCircuitComponent extends VisualComponent implements Container, CustomTouchable, StateObserver, ObservableHierarchy {

	private Color inputColor = VisualContact.inputColor;
	private Color outputColor = VisualContact.outputColor;

	double marginSize = 0.2;
	double contactLength = 1;
	double contactStep = 1;


	DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);


	private Rectangle2D contactLabelBB = null;
	protected Rectangle2D totalBB = null;

	public VisualCircuitComponent(CircuitComponent component) {
		super(component);
		component.addObserver(this);
		addPropertyDeclarations();

		// testing...

/*		addInput("Req_in", VisualContact.Direction.WEST);
		addOutput("Ack_out", VisualContact.Direction.WEST);
		addInput("Data_in",  VisualContact.Direction. WEST);
		addOutput("Req_out", VisualContact.Direction.EAST);
		addInput("Ack_in",   VisualContact.Direction. EAST);

		addOutput("Data_out", VisualContact.Direction.NORTH);
		addOutput("Data out 2", VisualContact.Direction.NORTH);
		addOutput("Reset",    VisualContact.Direction.SOUTH);*/
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration(this, "Name", "getName", "setName", String.class));
	}

	// updates sequential position of the contacts
	private void updateStepPosition() {
		int north=0;
		int south=0;
		int east=0;
		int west=0;
		for (Node n: this.getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact vc = (VisualContact)n;
				if (vc.getDirection().equals(Direction.EAST)) east++;
				if (vc.getDirection().equals(Direction.SOUTH)) south++;
				if (vc.getDirection().equals(Direction.NORTH)) north++;
				if (vc.getDirection().equals(Direction.WEST)) west++;
			}
		}

		double eastStep=-contactStep*(east-1)/2;
		double westStep=-contactStep*(west-1)/2;
		double northStep=-contactStep*(north-1)/2;
		double southStep=-contactStep*(south-1)/2;

		for (Node n: getChildren()) {
			if (!(n instanceof VisualContact)) continue;
			VisualContact vc=(VisualContact)n;
			switch (vc.getDirection()) {
			case EAST:
				vc.setY(eastStep);
				eastStep+=contactStep;
				break;
			case WEST:
				vc.setY(westStep);
				westStep+=contactStep;
				break;
			case SOUTH:
				vc.setX(southStep);
				southStep+=contactStep;
				break;
			case NORTH:
				vc.setX(northStep);
				northStep+=contactStep;
				break;
			}
		}
	}

	private void updateSidePosition(Rectangle2D labelBB, VisualContact contact) {

		double side_pos_w = (double)(Math.round((labelBB.getMinX()-contactLength)*2))/2;
		double side_pos_e = (double)(Math.round((labelBB.getMaxX()+contactLength)*2))/2;
		double side_pos_s = (double)(Math.round((labelBB.getMaxY()+contactLength)*2))/2;
		double side_pos_n = (double)(Math.round((labelBB.getMinY()-contactLength)*2))/2;

		for (Node vn: groupImpl.getChildren()) {
			if (vn instanceof VisualContact) {
				VisualContact vc = (VisualContact)vn;
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
	}

	public void updateDirection(VisualContact vc, VisualContact.Direction dir) {
		vc.setDirection(dir);
		contactLabelBB = null;
	}

	public void addContact(VisualContact vc) {
		if (!getChildren().contains(vc)) {
			((CircuitComponent)this.getReferencedComponent()).add(vc.getReferencedComponent());
			add(vc);
			updateStepPosition();

			contactLabelBB = null;
		}
	}

	protected void updateTotalBB() {

		totalBB = BoundingBoxHelper.mergeBoundingBoxes(Hierarchy.getChildrenOfType(this, Touchable.class));

		if (contactLabelBB!=null&&totalBB!=null)
			Rectangle2D.union(totalBB, contactLabelBB, totalBB);
	}

	protected Rectangle2D getContactLabelBB(Graphics2D g) {
		int north=0;
		int south=0;
		int east=0;
		int west=0;
		for (Node n: this.getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact vc = (VisualContact)n;
				if (vc.getDirection().equals(Direction.EAST)) east++;
				if (vc.getDirection().equals(Direction.SOUTH)) south++;
				if (vc.getDirection().equals(Direction.NORTH)) north++;
				if (vc.getDirection().equals(Direction.WEST)) west++;
			}
		}


		if (contactLabelBB==null) {
			Rectangle2D cur;
			double xx;
			double width_w=0;
			double width_e=0;
			double width_n=0;
			double width_s=0;

			for (Node vn: getChildren()) {
				if (!(vn instanceof VisualContact)) continue;
				VisualContact c = (VisualContact)vn;
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

			double height = Math.max(east, west)*contactStep+width_n+width_s+marginSize*4;
			double width = Math.max(north, south)*contactStep+width_e+width_w+marginSize*4;

			contactLabelBB = new Rectangle2D.Double(-width/2, -height/2, width, height);
			updateSidePosition(contactLabelBB, null);
			updateTotalBB();
		}
		return contactLabelBB;
	}

	protected void drawContactConnections(Graphics2D g) {
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));

		Rectangle2D BB = getContactLabelBB(g);


		for (Node n: getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact vc=(VisualContact)n;
				if (vc.getDirection().equals(Direction.EAST)) {
					Line2D line = new Line2D.Double(vc.getX(), vc.getY(), BB.getMaxX(), vc.getY());
					g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), getColorisation()));
					g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
					g.draw(line);
				}
				if (vc.getDirection().equals(Direction.WEST)) {
					Line2D line = new Line2D.Double(vc.getX(), vc.getY(), BB.getMinX(), vc.getY());
					g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), getColorisation()));
					g.draw(line);
				}

				if (vc.getDirection().equals(Direction.NORTH)) {
					Line2D line = new Line2D.Double(vc.getX(), vc.getY(), vc.getX(), BB.getMinY());
					g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), getColorisation()));
					g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
					g.draw(line);
				}

				if (vc.getDirection().equals(Direction.SOUTH)) {
					Line2D line = new Line2D.Double(vc.getX(), vc.getY(), vc.getX(), BB.getMaxY());
					g.setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), getColorisation()));
					g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
					g.draw(line);
				}
			}
		}

	}


	protected void drawContacts(Graphics2D g) {

		Rectangle2D cur;

		Rectangle2D BB = getContactLabelBB(g);

		double step_pos;

		for (Node n: getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact)n;
				if (!c.getDirection().equals(Direction.WEST)) continue;

				GlyphVector gv = c.getNameGlyphs(g);
				cur = gv.getVisualBounds();
				g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, getColorisation()));
				step_pos = c.getY();

				g.drawGlyphVector(gv, (float)(BB.getMinX()+marginSize), (float)(step_pos+(cur.getHeight())/2));
			}
		}

		for (Node n: getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact)n;
				if (!c.getDirection().equals(Direction.EAST)) continue;
				GlyphVector gv = c.getNameGlyphs(g);
				cur = gv.getVisualBounds();
				g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, getColorisation()));

				step_pos = c.getY();
				g.drawGlyphVector(gv, (float)(BB.getMaxX()-marginSize-cur.getWidth()), (float)(step_pos+(cur.getHeight())/2));
			}
		}

		AffineTransform at = new AffineTransform();
		at.quadrantRotate(-1);
		//
		g.transform(at);

		for (Node n: getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact)n;
				if (!c.getDirection().equals(Direction.NORTH)) continue;

				GlyphVector gv = c.getNameGlyphs(g);
				cur = gv.getVisualBounds();
				g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, getColorisation()));

				step_pos = c.getX();
				g.drawGlyphVector(gv, (float)(BB.getMaxY()-marginSize-cur.getWidth()), (float)(step_pos+(cur.getHeight())/2));
			}
		}

		for (Node n: getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact)n;
				if (!c.getDirection().equals(Direction.SOUTH)) continue;

				GlyphVector gv = c.getNameGlyphs(g);
				cur = gv.getVisualBounds();
				g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, getColorisation()));

				step_pos = c.getX();
				g.drawGlyphVector(gv, (float)(BB.getMinY()+marginSize), (float)(step_pos+(cur.getHeight())/2));
			}
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
	public void add(Node node) {
		groupImpl.add(node);
		if (node instanceof VisualContact) {
			((VisualContact)node).addObserver(this);
		}
	}

	public Collection<Node> getChildren() {
		return groupImpl.getChildren();
	}

	public Node getParent() {
		return groupImpl.getParent();
	}

	public void setParent(Node parent) {
		groupImpl.setParent(parent);
	}

	public void remove(Node node) {
		if (node instanceof VisualContact) {
			contactLabelBB = null;
		}

		groupImpl.remove(node);
	}

	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
		for (Node node: nodes) {
			if (node instanceof VisualContact) {
				((VisualContact)node).addObserver(this);
			}
		}
	}


	public void remove(Collection<Node> nodes) {
		for (Node n: nodes) {
			remove(n);
		}
	}


	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}


	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

	public void setColorisation(Color color) {
		super.setColorisation(color);
		for (Colorisable node : Hierarchy.getChildrenOfType(this, Colorisable.class))
			node.setColorisation(color);
	}

	@Override
	public Node customHitTest(Point2D point) {
		Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
		for(Node vn : getChildren())
			if (vn instanceof VisualNode)
				if(((VisualNode)vn).hitTest(pointInLocalSpace))
					return vn;

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

			if (dir!=vc.getDirection()) {
	 			vc.setDirection(dir);
				contactLabelBB = null;
			}

		}
		if (e instanceof PropertyChangedEvent) {
			PropertyChangedEvent pc = (PropertyChangedEvent)e;
			if (pc.getPropertyName().equals("name")||
				pc.getPropertyName().equals("IOtype")||
				pc.getPropertyName().equals("direction")
					) {
				contactLabelBB = null;
			}
		}
	}

	public VisualContact addInput(String name, VisualContact.Direction dir) {

		if (dir==null) dir=VisualContact.Direction.WEST;

		Contact c = new Contact(IOType.INPUT);

		VisualContact vc = new VisualContact(c, dir, name);
		addContact(vc);

		return vc;
	}

	public VisualContact addOutput(String name, VisualContact.Direction dir) {
		if (dir==null) dir=VisualContact.Direction.EAST;

		Contact c = new Contact(IOType.OUTPUT);
		VisualContact vc = new VisualContact(c, dir, name);
		addContact(vc);
		return vc;
	}

	@Override
	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
	}

	@Override
	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}

	public String getName() {
		return ((CircuitComponent)getReferencedComponent()).getName();
	}

	public void setName(String name) {
		((CircuitComponent)getReferencedComponent()).setName(name);
	}

}
