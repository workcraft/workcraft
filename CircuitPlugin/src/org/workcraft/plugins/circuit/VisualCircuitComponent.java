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
import java.lang.ref.WeakReference;
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.CustomTouchable;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
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

@DisplayName("Abstract Component")
@Hotkey(KeyEvent.VK_A)
@SVGIcon("images/icons/svg/circuit-component.svg")
public class VisualCircuitComponent extends VisualComponent implements
		Container, CustomTouchable, StateObserver, ObservableHierarchy {
	private Color inputColor = VisualContact.inputColor;
	private Color outputColor = VisualContact.outputColor;

	double marginSize = 0.2;
	double contactLength = 1;
	double contactStep = 1;

	protected Rectangle2D internalBB = null;
	private WeakReference<VisualContact> mainContact = null;

	DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);
	RenderType renderType = RenderType.BOX;

	public VisualCircuitComponent(CircuitComponent component) {
		super(component, true, true, true);
		component.addObserver(this);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualCircuitComponent, Boolean>(
				this, "Treat as environment", Boolean.class) {
			protected void setter(VisualCircuitComponent object, Boolean value) {
				object.getReferencedCircuitComponent().setIsEnvironment(value);
			}

			protected Boolean getter(VisualCircuitComponent object) {
				return object.getReferencedCircuitComponent()
						.getIsEnvironment();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualCircuitComponent, RenderType>(
				this, "Render type", RenderType.class, RenderType.getChoice()) {
			protected void setter(VisualCircuitComponent object,
					RenderType value) {
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
		if (mainContact != null)
			ret = mainContact.get();
		if (ret == null) {
			for (Node n : getChildren()) {
				if (n instanceof VisualContact) {
					if (((VisualContact) n).getIOType() == IOType.OUTPUT) {
						setMainContact((VisualContact) n);
						ret = (VisualContact) n;
						break;
					}
				}
			}
		}
		return ret;
	}

	public CircuitComponent getReferencedCircuitComponent() {
		return (CircuitComponent) this.getReferencedComponent();
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
		invalidateBoundingBox();
		updateStepPositions();
		sendNotification(new PropertyChangedEvent(this, "render type"));
	}

	// updates sequential position of the contacts
	protected void updateStepPositions() {
		int northCount = 0;
		int southCount = 0;
		int eastCount = 0;
		int westCount = 0;
		for (Node n : this.getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact vc = (VisualContact) n;
				switch (vc.getDirection()) {
				case NORTH: northCount++; break;
				case EAST: eastCount++; break;
				case SOUTH: southCount++; break;
				case WEST: westCount++; break;
				}
			}
		}

		double eastStep = -contactStep * (eastCount - 1) / 2;
		double westStep = -contactStep * (westCount - 1) / 2;
		double northStep = -contactStep * (northCount - 1) / 2;
		double southStep = -contactStep * (southCount - 1) / 2;
		for (Node n : getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact vc = (VisualContact) n;
				switch (vc.getDirection()) {
				case EAST:
					vc.setY(eastStep);
					eastStep += contactStep;
					break;
				case WEST:
					vc.setY(westStep);
					westStep += contactStep;
					break;
				case SOUTH:
					vc.setX(southStep);
					southStep += contactStep;
					break;
				case NORTH:
					vc.setX(northStep);
					northStep += contactStep;
					break;
				}
			}
		}
	}

	public static double snapP5(double x) {
		return (double) (Math.round(x * 2)) / 2;
	}

	private void updateSidePosition(VisualContact vc) {
		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
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

	public void updateDirection(VisualCircuit vcircuit, VisualContact vc, VisualContact.Direction dir) {
		vc.setDirection(dir);
		invalidateBoundingBox();
		updateSidePosition(vc);
		updateStepPositions();
	}

	public void addContact(VisualCircuit vcircuit, VisualContact vc) {
		if (!getChildren().contains(vc)) {
			Container container = AbstractVisualModel.getMathContainer(vcircuit, this);
			container.add(vc.getReferencedComponent());
			add(vc);
			invalidateBoundingBox();
			updateSidePosition(vc);
			updateStepPositions();
		}
	}


	private void invalidateBoundingBox() {
		internalBB = null;
	}

	public void updateBoundingBox(DrawRequest r) {
		if (internalBB != null) return;

		int northCount = 0;
		int eastCount = 0;
		int southCount = 0;
		int westCount = 0;
		for (Node n : this.getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact vc = (VisualContact) n;
				switch (vc.getDirection()) {
				case NORTH: northCount++; break;
				case EAST: eastCount++; break;
				case SOUTH: southCount++; break;
				case WEST: westCount++; break;
				}
			}
		}

		double northSize = 0;
		double eastSize = 0;
		double southSize = 0;
		double westSize = 0;
		for (Node vn : getChildren()) {
			if (vn instanceof VisualContact) {
				VisualContact vc = (VisualContact)vn;
				Rectangle2D labelBB = vc.getNameGlyphs(r).getVisualBounds();
				double labelWidth = (double) (Math.round(labelBB.getWidth() * 4)) / 4;
				switch (vc.getDirection()) {
				case NORTH:	northSize = Math.max(labelWidth, northSize); break;
				case EAST: eastSize = Math.max(labelWidth, eastSize); break;
				case SOUTH:	southSize = Math.max(labelWidth, southSize); break;
				case WEST: westSize = Math.max(labelWidth, westSize); break;
				}
			}
		}

		double w = Math.max(northCount, southCount) * contactStep + eastSize + westSize	+ marginSize * 4;
		double h = Math.max(eastCount, westCount) * contactStep + northSize + southSize + marginSize * 4;
		double x = - w / 2;
		double y = - h / 2;

		Rectangle2D  minimalBoundinBox = new Rectangle2D.Double(x, y, w, h);
		Rectangle2D contactInscribedBox = getContactInscribedBox();
		minimalBoundinBox = BoundingBoxHelper.union(minimalBoundinBox, contactInscribedBox);
		if (minimalBoundinBox != null) {
			double expW = minimalBoundinBox.getWidth();
			if (eastCount > 0) expW -= size;
			if (westCount > 0) expW -= size;
			if (expW > w) {
				w = expW;
				x = minimalBoundinBox.getX();
				if (westCount > 0) x += size;
			}
			double expH = minimalBoundinBox.getHeight();
			if (northCount > 0) expH -= size;
			if (southCount > 0) expH -= size;
			if (expH > h) {
				h = expH;
				y = minimalBoundinBox.getY();
				if (northCount > 0) y += size;
			}
		}
		internalBB = new Rectangle2D.Double(x, y, w, h);
	}

	private Rectangle2D getContactInscribedBox() {
		Collection<Touchable> touchableChildren = Hierarchy.getChildrenOfType(this, Touchable.class);
		Rectangle2D bb = BoundingBoxHelper.mergeBoundingBoxes(touchableChildren);
		if (bb != null) {
			double x1 = bb.getMinX();
			double y1 = bb.getMinY();
			double x2 = bb.getMaxX();
			double y2 = bb.getMaxY();
			for (Node vn : getChildren()) {
				if (vn instanceof VisualContact) {
					VisualContact vc = (VisualContact)vn;
					switch (vc.getDirection()) {
					case NORTH:	y1 = Math.max(y1, vc.getBoundingBox().getMaxY()); break;
					case EAST: x2 = Math.min(x2, vc.getBoundingBox().getMinX()); break;
					case SOUTH:	y2 = Math.min(y2, vc.getBoundingBox().getMinY()); break;
					case WEST: x1 = Math.max(x1, vc.getBoundingBox().getMaxX()); break;
					}
				}
			}
			bb = new Rectangle2D.Double(x1, y1, x2-x1, y2-y1);
		}
		return bb;
	}

	private Line2D getContactLine(Rectangle2D bb, VisualContact vc) {
		switch (vc.getDirection()) {
		case EAST: return new Line2D.Double(vc.getX(), vc.getY(), bb.getMaxX(), vc.getY());
		case WEST: return new Line2D.Double(vc.getX(), vc.getY(), bb.getMinX(), vc.getY());
		case NORTH: return new Line2D.Double(vc.getX(), vc.getY(), vc.getX(), bb.getMinY());
		case SOUTH: return new Line2D.Double(vc.getX(), vc.getY(), vc.getX(), bb.getMaxY());
		default: return null;
		}
	}

	protected void drawContactConnections(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		g.setStroke(new BasicStroke((float) CircuitSettings.getWireWidth()));
		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		for (Node n : getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact vc = (VisualContact) n;
				Line2D line = getContactLine(bb, vc);
				if (line != null) {
					g.setColor(Coloriser.colorise(CommonVisualSettings.getBorderColor(), colorisation));
					g.draw(line);
				}
			}
		}
	}

	protected void drawContacts(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();

		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		for (Node n : getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact) n;
				if (c.getDirection().equals(Direction.WEST)) {
					GlyphVector gv = c.getNameGlyphs(r);
					Rectangle2D labelBB = gv.getVisualBounds();
					Color color = (c.getIOType() == IOType.INPUT) ? inputColor : outputColor;
					g.setColor(Coloriser.colorise(color, colorisation));
					float x = (float) (bb.getMinX() + marginSize);
					float y = (float) (c.getY() + labelBB.getHeight() / 2);
					g.drawGlyphVector(gv, x, y);
				}
			}
		}

		for (Node n : getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact) n;
				if (c.getDirection().equals(Direction.EAST)) {
					GlyphVector gv = c.getNameGlyphs(r);
					Rectangle2D labelBB = gv.getVisualBounds();
					Color color = (c.getIOType() == IOType.INPUT) ? inputColor : outputColor;
					g.setColor(Coloriser.colorise(color, colorisation));
					float x = (float) (bb.getMaxX() - marginSize - labelBB.getWidth());
					float y = (float) (c.getY() + labelBB.getHeight() / 2);
					g.drawGlyphVector(gv, x, y);
				}
			}
		}

		AffineTransform at = new AffineTransform();
		at.quadrantRotate(-1);
		g.transform(at);

		for (Node n : getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact) n;
				if (c.getDirection().equals(Direction.NORTH)) {
					GlyphVector gv = c.getNameGlyphs(r);
					Rectangle2D labelBB = gv.getVisualBounds();
					Color color = (c.getIOType() == IOType.INPUT) ? inputColor : outputColor;
					g.setColor(Coloriser.colorise(color, colorisation));
					float x = (float) (bb.getMinY() + marginSize + labelBB.getWidth());
					float y = (float) (c.getX() + labelBB.getHeight() / 2);
					g.drawGlyphVector(gv, -x, y);
				}
			}
		}

		for (Node n : getChildren()) {
			if (n instanceof VisualContact) {
				VisualContact c = (VisualContact) n;
				if (c.getDirection().equals(Direction.SOUTH)) {
					GlyphVector gv = c.getNameGlyphs(r);
					Rectangle2D labelBB = gv.getVisualBounds();
					Color color = (c.getIOType() == IOType.INPUT) ? inputColor : outputColor;
					g.setColor(Coloriser.colorise(color, colorisation));
					float x = (float) (bb.getMaxY() - marginSize);
					float y = (float) (c.getX() + labelBB.getHeight() / 2);
					g.drawGlyphVector(gv, -x, y);
				}
			}
		}
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();

		cacheRenderedText(r); // needed to better estimate the bounding box
		updateBoundingBox(r);
		drawContactConnections(r);

		Rectangle2D shape = getInternalBoundingBoxInLocalSpace();
		g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));

		if (!getIsEnvironment()) {
			g.setStroke(new BasicStroke((float) CircuitSettings.getBorderWidth()));
		} else {
			float dash[] = { 0.25f, 0.25f };
			g.setStroke(new BasicStroke((float) CircuitSettings.getBorderWidth(),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f));
		}
		g.draw(shape);
		drawLabelInLocalSpace(r);
		drawNameInLocalSpace(r);
		drawContacts(r);
	}

	@Override
	public Rectangle2D getInternalBoundingBoxInLocalSpace() {
		Rectangle2D bb = BoundingBoxHelper.copy(internalBB);
		if (bb == null) {
			bb = super.getInternalBoundingBoxInLocalSpace();
		}
		return bb;
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D bb = super.getBoundingBoxInLocalSpace();
		Collection<Touchable> touchableChildren = Hierarchy.getChildrenOfType(this, Touchable.class);
		Rectangle2D childrenBB = BoundingBoxHelper.mergeBoundingBoxes(touchableChildren);
		return BoundingBoxHelper.union(bb, childrenBB);
	}

	@Override
	public void add(Node node) {
		groupImpl.add(node);
		if (node instanceof VisualContact) {
			((VisualContact) node).addObserver(this);
		}
	}

	@Override
	public Collection<Node> getChildren() {
		return groupImpl.getChildren();
	}

	@Override
	public Node getParent() {
		return groupImpl.getParent();
	}

	@Override
	public void setParent(Node parent) {
		groupImpl.setParent(parent);
	}

	@Override
	public void remove(Node node) {
		if (node instanceof VisualContact) {
			invalidateBoundingBox();
		}
		groupImpl.remove(node);
	}

	@Override
	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
		for (Node node : nodes) {
			if (node instanceof VisualContact) {
				((VisualContact) node).addObserver(this);
			}
		}
	}

	@Override
	public void remove(Collection<Node> nodes) {
		for (Node n : nodes) {
			remove(n);
		}
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}

	@Override
	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

	@Override
	public Node customHitTest(Point2D point) {
		Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
		for (Node vn : getChildren()) {
			if (vn instanceof VisualNode) {
				if (((VisualNode) vn).hitTest(pointInLocalSpace)) {
					return vn;
				}
			}
		}

		if (hitTest(point)) {
			return this;
		} else {
			return null;
		}
	}

	@Override
	public void notify(StateEvent e) {
		if (e instanceof TransformChangedEvent) {
			TransformChangedEvent t = (TransformChangedEvent) e;
			VisualContact vc = (VisualContact) t.sender;

			AffineTransform at = t.sender.getTransform();
			double x = at.getTranslateX();
			double y = at.getTranslateY();

			Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
			VisualContact.Direction dir = vc.getDirection();
			if ((x < bb.getMinX()) && (y > bb.getMinY()) && (y < bb.getMaxY())) {
				dir = Direction.WEST;
			}
			if ((x > bb.getMaxX()) && (y > bb.getMinY()) && (y < bb.getMaxY())) {
				dir = Direction.EAST;
			}
			if ((y < bb.getMinY()) && (x > bb.getMinX()) && (x < bb.getMaxX())) {
				dir = Direction.NORTH;
			}
			if ((y > bb.getMaxY()) && (x > bb.getMinX()) && (x < bb.getMaxX())) {
				dir = Direction.SOUTH;
			}
			if (dir != vc.getDirection()) {
				vc.setDirection(dir);
				invalidateBoundingBox();
			}

		}
		if (e instanceof PropertyChangedEvent) {
			PropertyChangedEvent pc = (PropertyChangedEvent) e;
			if (pc.getPropertyName().equals("name")
					|| pc.getPropertyName().equals("IOtype")
					|| pc.getPropertyName().equals("direction")
					|| pc.getPropertyName().equals("setFunction")
					|| pc.getPropertyName().equals("resetFunction")) {

				invalidateBoundingBox();
				for (Node n : getChildren()) {
					if (n instanceof VisualFunctionContact) {
						VisualFunctionContact c = (VisualFunctionContact) n;
						c.resetRenderedFormula();
						c.resetNameGlyph();
					}
				}
			}
		}
	}

	@Override
	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
	}

	@Override
	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}

	@Override
	public void removeAllObservers() {
		groupImpl.removeAllObservers();
	}

}
