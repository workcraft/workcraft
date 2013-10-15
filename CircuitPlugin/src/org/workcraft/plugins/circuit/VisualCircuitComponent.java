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
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.CustomTouchable;
import org.workcraft.dom.visual.DrawRequest;
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
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Hierarchy;

//@VisualClass("org.workcraft.plugins.circuit.VisualCircuitComponent")

@DisplayName("Abstract Component")
@Hotkey(KeyEvent.VK_A)
@SVGIcon("images/icons/svg/circuit-component.svg")

public class VisualCircuitComponent extends VisualComponent implements Container, CustomTouchable, StateObserver, ObservableHierarchy {
	private Color inputColor = VisualContact.inputColor;
	private Color outputColor = VisualContact.outputColor;

	protected static Font nameFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	private GlyphVector nameGlyphs = null;
	private String glyphsForName = null;
	private Point2D namePosition = null;

	double marginSize = 0.2;
	double contactLength = 1;
	double contactStep = 1;

	protected Rectangle2D contactLabelBB = null;
	protected Rectangle2D totalBB = null;
	private WeakReference<VisualContact> mainContact = null;

	DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);
	RenderType renderType = RenderType.BOX;

	public VisualCircuitComponent(CircuitComponent component) {
		super(component);
		component.addObserver(this);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualCircuitComponent, String>(
				this, "Name", String.class) {
			protected void setter(VisualCircuitComponent object, String value) {
				object.getReferencedCircuitComponent().setName(value);
			}
			protected String getter(VisualCircuitComponent object) {
				return object.getReferencedCircuitComponent().getName();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualCircuitComponent, Boolean>(
				this, "Treat as environment", Boolean.class) {
			protected void setter(VisualCircuitComponent object, Boolean value) {
				object.getReferencedCircuitComponent().setIsEnvironment(value);
			}
			protected Boolean getter(VisualCircuitComponent object) {
				return object.getReferencedCircuitComponent().getIsEnvironment();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualCircuitComponent, RenderType>(
				this, "Render type", RenderType.class, RenderType.getChoice()) {
			protected void setter(VisualCircuitComponent object, RenderType value) {
				object.setRenderType(value);
			}
			protected RenderType getter(VisualCircuitComponent object) {
				return object.getRenderType();
			}
		});
	}
	public void setMainContact(VisualContact contact) {
		this.mainContact = new WeakReference<VisualContact>(contact);
	}

	public VisualContact getMainContact() {
		VisualContact ret = null;
		if (mainContact!=null) ret=mainContact.get();
		if (ret==null) {
			for (Node  n : getChildren()) {
				if (n instanceof VisualContact) {
					if (((VisualContact)n).getIOType()==IOType.OUTPUT) {
						setMainContact((VisualContact)n);
						ret = (VisualContact)n;
						break;
					}
				}
			}
		}
		return ret;
	}

	public CircuitComponent getReferencedCircuitComponent() {
		return (CircuitComponent)this.getReferencedComponent();
	}

	public String getName() {
		return getReferencedCircuitComponent().getName();
	}

	public void setName(String value) {
		getReferencedCircuitComponent().setName(value);
	}

	public boolean getIsEnvironment() {
		if (getReferencedCircuitComponent() != null) {
			return getReferencedCircuitComponent().getIsEnvironment();
		}
		return false;
	}

	public void setIsEnvironment(boolean isEnvironment) {
		if (getReferencedCircuitComponent() != null) {
			getReferencedCircuitComponent().setIsEnvironment(isEnvironment);
		}
	}

	public RenderType getRenderType() {
		return renderType;
	}

	public void setRenderType(RenderType renderType) {
		this.renderType = renderType;
		contactLabelBB = null;
		updateStepPositions();
		updateTotalBB();
	}

	public GlyphVector getNameGlyphs(Graphics2D g) {
		updateNameGlyph(g);
		return nameGlyphs;
	}

	public Rectangle2D getNameBB(Graphics2D g) {
		return getNameGlyphs(g).getVisualBounds();
	}

	protected void drawLabelInLocalSpace(DrawRequest r) {
		updateNameGlyph(r.getGraphics());
		r.getGraphics().setColor(Coloriser.colorise(CommonVisualSettings.getBorderColor(), r.getDecoration().getColorisation()));
		r.getGraphics().setFont(nameFont);
		r.getGraphics().drawString(getName(), (float) namePosition.getX(), (float) namePosition.getY());
	}

	protected void updateNameGlyph(Graphics2D g) {
		if (nameGlyphs == null || !getName().equals(glyphsForName)) {
			final GlyphVector glyphs = nameFont.createGlyphVector(
					g.getFontRenderContext(), getName());
			glyphsForName = getName();
			nameGlyphs = glyphs;
		}
	}

	protected void drawNameInLocalSpace(DrawRequest r) {
		updateNameGlyph(r.getGraphics());

		r.getGraphics().setColor(Coloriser.colorise(CommonVisualSettings.getBorderColor(), r.getDecoration().getColorisation()));

		r.getGraphics().setFont(nameFont);
		if (contactLabelBB!=null)
			r.getGraphics().drawString(getName(), (float)(contactLabelBB.getMaxX()-0.2),
				(float)(contactLabelBB.getMaxY()+0.5));
	}

	// updates sequential position of the contacts
	protected void updateStepPositions() {
		int north = 0;
		int south = 0;
		int east = 0;
		int west = 0;
		for (Node n: this.getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact vc = (VisualContact)n;
				if (vc.getDirection().equals(Direction.EAST)) east++;
				if (vc.getDirection().equals(Direction.SOUTH)) south++;
				if (vc.getDirection().equals(Direction.NORTH)) north++;
				if (vc.getDirection().equals(Direction.WEST)) west++;
			}
		}

		double eastStep = -contactStep * (east - 1) / 2;
		double westStep = -contactStep * (west - 1) / 2;
		double northStep = -contactStep * (north - 1) / 2;
		double southStep = -contactStep * (south - 1) / 2;

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

	public static double snapP5(double x) {
		return (double)(Math.round(x * 2)) / 2;
	}

	private void updateSidePosition(VisualContact vc) {
		Rectangle2D bb = getBestBB();
		switch (vc.getDirection()) {
			case EAST:
				vc.setX(snapP5(bb.getMaxX() + contactLength));
				break;
			case WEST:
				vc.setX(snapP5(bb.getMinX() - contactLength));
				break;
			case NORTH:
				vc.setY(snapP5(bb.getMinY() - contactLength));
				break;
			case SOUTH:
				vc.setY(snapP5(bb.getMaxY() + contactLength));
				break;
		}
	}

	public void updateDirection(VisualContact vc, VisualContact.Direction dir) {
		vc.setDirection(dir);
		contactLabelBB = null;
		updateSidePosition(vc);
		updateStepPositions();
	}

	public void addContact(VisualContact vc) {
		if (!getChildren().contains(vc)) {
			this.getReferencedCircuitComponent().add(vc.getReferencedComponent());
			add(vc);
			contactLabelBB = null;
			updateSidePosition(vc);
			updateStepPositions();
		}
	}

	protected void updateTotalBB() {
		totalBB = BoundingBoxHelper.mergeBoundingBoxes(Hierarchy.getChildrenOfType(this, Touchable.class));
		if (contactLabelBB != null && totalBB != null) {
			Rectangle2D.union(totalBB, contactLabelBB, totalBB);
		}
	}

	public Rectangle2D getBestBB() {
		Rectangle2D result = contactLabelBB;
		if (result==null) {
			result = new Rectangle2D.Double(-0.5, -0.5, 1, 1);
		}
		return result;
	}

	public void updateContactLabelBB(Graphics2D g) {
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

		double width_w=0;
		double width_e=0;
		double width_n=0;
		double width_s=0;
		for (Node vn: getChildren()) {
			if (!(vn instanceof VisualContact)) continue;
			VisualContact vc = (VisualContact)vn;
			GlyphVector gv = vc.getNameGlyphs(g);
			Rectangle2D cur = gv.getVisualBounds();
			double xx = (double)(Math.round(cur.getWidth() * 4))/4;

			switch (vc.getDirection()) {
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
		updateTotalBB();
	}

	protected void drawContactConnections(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		g.setStroke(new BasicStroke((float)CircuitSettings.getCircuitWireWidth()));
		Rectangle2D bb = getBestBB();
		for (Node n: getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact vc=(VisualContact)n;
				if (vc.getDirection().equals(Direction.EAST)) {
					Line2D line = new Line2D.Double(vc.getX(), vc.getY(), bb.getMaxX(), vc.getY());
					g.setColor(Coloriser.colorise(CommonVisualSettings.getBorderColor(), colorisation));
					g.draw(line);
				}
				if (vc.getDirection().equals(Direction.WEST)) {
					Line2D line = new Line2D.Double(vc.getX(), vc.getY(), bb.getMinX(), vc.getY());
					g.setColor(Coloriser.colorise(CommonVisualSettings.getBorderColor(), colorisation));
					g.draw(line);
				}

				if (vc.getDirection().equals(Direction.NORTH)) {
					Line2D line = new Line2D.Double(vc.getX(), vc.getY(), vc.getX(), bb.getMinY());
					g.setColor(Coloriser.colorise(CommonVisualSettings.getBorderColor(), colorisation));
					g.draw(line);
				}

				if (vc.getDirection().equals(Direction.SOUTH)) {
					Line2D line = new Line2D.Double(vc.getX(), vc.getY(), vc.getX(), bb.getMaxY());
					g.setColor(Coloriser.colorise(CommonVisualSettings.getBorderColor(), colorisation));
					g.draw(line);
				}
			}
		}
	}

	protected void drawContacts(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		Rectangle2D bb = getBestBB();
		double step_pos;
		for (Node n: getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact)n;
				if (!c.getDirection().equals(Direction.WEST)) continue;
				GlyphVector gv = c.getNameGlyphs(g);
				Rectangle2D cur = gv.getVisualBounds();
				g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, colorisation));
				step_pos = c.getY();
				g.drawGlyphVector(gv, (float)(bb.getMinX()+marginSize), (float)(step_pos+(cur.getHeight())/2));
			}
		}

		for (Node n: getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact)n;
				if (!c.getDirection().equals(Direction.EAST)) continue;
				GlyphVector gv = c.getNameGlyphs(g);
				Rectangle2D cur = gv.getVisualBounds();
				g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, colorisation));
				step_pos = c.getY();
				g.drawGlyphVector(gv, (float)(bb.getMaxX()-marginSize-cur.getWidth()), (float)(step_pos+(cur.getHeight())/2));
			}
		}

		AffineTransform at = new AffineTransform();
		at.quadrantRotate(-1);
		g.transform(at);

		for (Node n: getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact)n;
				if (!c.getDirection().equals(Direction.NORTH)) continue;
				GlyphVector gv = c.getNameGlyphs(g);
				Rectangle2D cur = gv.getVisualBounds();
				g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, colorisation));
				step_pos = c.getX();
				g.drawGlyphVector(gv, (float)(bb.getMaxY()-marginSize-cur.getWidth()), (float)(step_pos+(cur.getHeight())/2));
			}
		}

		for (Node n: getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact)n;
				if (!c.getDirection().equals(Direction.SOUTH)) continue;
				GlyphVector gv = c.getNameGlyphs(g);
				Rectangle2D cur = gv.getVisualBounds();
				g.setColor(Coloriser.colorise((c.getIOType()==IOType.INPUT)?inputColor:outputColor, colorisation));
				step_pos = c.getX();
				g.drawGlyphVector(gv, (float)(bb.getMinY()+marginSize), (float)(step_pos+(cur.getHeight())/2));
			}
		}
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		updateContactLabelBB(g);

		drawNameInLocalSpace(r);
		Color colorisation = r.getDecoration().getColorisation();
		drawContactConnections(r);

		Rectangle2D shape = getBestBB();

		g.setColor(Coloriser.colorise(CommonVisualSettings.getFillColor(), colorisation));
		g.fill(shape);
		g.setColor(Coloriser.colorise(CommonVisualSettings.getBorderColor(), colorisation));

		if (!getIsEnvironment()) {
			g.setStroke(new BasicStroke((float)CircuitSettings.getComponentBorderWidth()));
		} else {
			float dash[] = {0.25f, 0.25f};
			g.setStroke(new BasicStroke(
						(float)CircuitSettings.getComponentBorderWidth(),
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f)	);
		}
		g.draw(shape);

		drawContacts(r);

	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		if (totalBB==null) {
			updateTotalBB();
		}

		if (totalBB!=null) return totalBB;

		double size = CommonVisualSettings.getBaseSize();
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

			totalBB = null;
			Rectangle2D bb = getBestBB();

			VisualContact.Direction dir = vc.getDirection();

			if (x<bb.getMinX()&&y>bb.getMinY()&&y<bb.getMaxY()) dir = Direction.WEST;
			if (x>bb.getMaxX()&&y>bb.getMinY()&&y<bb.getMaxY()) dir = Direction.EAST;
			if (y<bb.getMinY()&&x>bb.getMinX()&&x<bb.getMaxX()) dir = Direction.NORTH;
			if (y>bb.getMaxY()&&x>bb.getMinX()&&x<bb.getMaxX()) dir = Direction.SOUTH;

			if (dir!=vc.getDirection()) {
	 			vc.setDirection(dir);
				contactLabelBB = null;
			}

		}
		if (e instanceof PropertyChangedEvent) {

			PropertyChangedEvent pc = (PropertyChangedEvent)e;
			if (pc.getPropertyName().equals("name")||
				pc.getPropertyName().equals("IOtype")||
				pc.getPropertyName().equals("direction")||
				pc.getPropertyName().equals("setFunction")||
				pc.getPropertyName().equals("resetFunction")) {

				contactLabelBB = null;

				for (Node n : getChildren()) {
					if (n instanceof VisualFunctionContact) {
						VisualFunctionContact vf = (VisualFunctionContact)n;
						vf.resetRenderedFormula();
						vf.resetNameGlyph();
					}
				}
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

}
