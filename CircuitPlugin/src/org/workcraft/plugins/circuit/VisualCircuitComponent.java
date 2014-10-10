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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

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
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@DisplayName("Abstract Component")
@Hotkey(KeyEvent.VK_A)
@SVGIcon("images/icons/svg/circuit-component.svg")
public class VisualCircuitComponent extends VisualComponent implements
		Container, CustomTouchable, StateObserver, ObservableHierarchy {
	private Color inputColor = VisualContact.inputColor;
	private Color outputColor = VisualContact.outputColor;

	double marginSize = 0.2;
	double contactLength = 0.5;
	double contactStep = 1.0;

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
		if (mainContact != null) {
			ret = mainContact.get();
		}
		if (ret == null) {
			for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
				if (vc.getIOType() == IOType.OUTPUT) {
					setMainContact(vc);
					ret = vc;
					break;
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
		if (this.renderType != renderType) {
			this.renderType = renderType;
			spreadContactsEvenly();
			invalidateBoundingBox();
			sendNotification(new PropertyChangedEvent(this, "render type"));
		}
	}

	private LinkedList<VisualContact> getOrderedContacts(final Direction dir, final boolean reverse) {
		LinkedList<VisualContact> list = new LinkedList<VisualContact>();
		for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
			if (vc.getDirection() == dir) {
				list.add(vc);
			}
		}
		Collections.sort(list, new Comparator<VisualContact>() {
			@Override
			public int compare(VisualContact vc1, VisualContact vc2) {
				if ((dir == Direction.NORTH) || (dir == Direction.SOUTH)) {
					return (reverse ? -1 : 1) * Double.compare(vc1.getX(), vc2.getX());
				} else {
					return (reverse ? -1 : 1) * Double.compare(vc1.getY(), vc2.getY());
				}
			}
		});
		return list;
	}

	private int getContactCount(final Direction dir) {
		int count = 0;
		for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
			if (vc.getDirection() == dir) {
				count++;
			}
		}
		return count;
	}

	private void spreadContactsEvenly() {
		int westCount = getContactCount(Direction.WEST);
		int northCount = getContactCount(Direction.NORTH);
		int eastCount = getContactCount(Direction.EAST);
		int southCount = getContactCount(Direction.SOUTH);

		double westPosition = -contactStep * (westCount - 1) / 2;
		double northPosition = -contactStep * (northCount - 1) / 2;
		double eastPosition = -contactStep * (eastCount - 1) / 2;
		double southPosition = -contactStep * (southCount - 1) / 2;
		for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
			switch (vc.getDirection()) {
			case WEST:
				vc.setY(westPosition);
				westPosition += contactStep;
				break;
			case NORTH:
				vc.setX(northPosition);
				northPosition += contactStep;
				break;
			case EAST:
				vc.setY(eastPosition);
				eastPosition += contactStep;
				break;
			case SOUTH:
				vc.setX(southPosition);
				southPosition += contactStep;
				break;
			}
		}
		invalidateBoundingBox();
	}

	public void setContactsDefaultPosition() {
		spreadContactsEvenly();
		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
			switch (vc.getDirection()) {
			case WEST:
				vc.setX(bb.getMinX() - contactLength);
				break;
			case NORTH:
				vc.setY(bb.getMinY() - contactLength);
				break;
			case EAST:
				vc.setX(bb.getMaxX() + contactLength);
				break;
			case SOUTH:
				vc.setY(bb.getMaxY() + contactLength);
				break;
			}
		}
		invalidateBoundingBox();
	}

	public static double snapP5(double x) {
		return (double) (Math.round(x * 2)) / 2;
	}

	public void addContact(VisualCircuit vcircuit, VisualContact vc) {
		if (!getChildren().contains(vc)) {
			LinkedList<VisualContact> sameSideContacts = getOrderedContacts(vc.getDirection(), true);
			Rectangle2D bb = getInternalBoundingBoxInLocalSpace();

			Container container = AbstractVisualModel.getMathContainer(vcircuit, this);
			container.add(vc.getReferencedComponent());
			add(vc);

			switch (vc.getDirection()) {
			case WEST:
				vc.setX(snapP5(bb.getMinX() - contactLength));
				if (sameSideContacts.size() > 0) {
					vc.setY(sameSideContacts.getFirst().getY() + contactLength);
				}
				break;
			case NORTH:
				vc.setY(snapP5(bb.getMinY() - contactLength));
				if (sameSideContacts.size() > 0) {
					vc.setX(sameSideContacts.getFirst().getX() + contactLength);
				}
				break;
			case EAST:
				vc.setX(snapP5(bb.getMaxX() + contactLength));
				if (sameSideContacts.size() > 0) {
					vc.setY(sameSideContacts.getFirst().getY() + contactLength);
				}
				break;
			case SOUTH:
				vc.setY(snapP5(bb.getMaxY() + contactLength));
				if (sameSideContacts.size() > 0) {
					vc.setX(sameSideContacts.getFirst().getX() + contactLength);
				}
				break;
			}
			invalidateBoundingBox();
		}
	}

	private void invalidateBoundingBox() {
		internalBB = null;
	}

	private Rectangle2D getContactMinimalBox() {
		double x1 = 0.0;
		double y1 = 0.0;
		double x2 = 0.0;
		double y2 = 0.0;

		boolean westFirst = true;
		boolean northFirst = true;
		boolean eastFirst = true;
		boolean southFirst = true;

		for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
			double x = vc.getBoundingBox().getCenterX();
			double y = vc.getBoundingBox().getCenterY();
			switch (vc.getDirection()) {
			case WEST:
				if (westFirst && eastFirst) {
					y1 = y - contactStep / 2;
					y2 = y + contactStep / 2;
				} else {
					y1 = Math.min(y1, y - contactStep / 2);
					y2 = Math.max(y2, y + contactStep / 2);
				}
				if (westFirst && northFirst && southFirst) {
					x1 = x + contactLength;
				}
				westFirst = false;
				break;
			case NORTH:
				if (northFirst && southFirst) {
					x1 = x - contactStep / 2;
					x2 = x + contactStep / 2;
				} else {
					x1 = Math.min(x1, x - contactStep / 2);
					x2 = Math.max(x2, x + contactStep / 2);
				}
				if (northFirst && westFirst && eastFirst) {
					y1 = y + contactLength;
				}
				northFirst = false;
				break;
			case EAST:
				if (eastFirst && westFirst) {
					y1 = y - contactStep / 2;
					y2 = y + contactStep / 2;
				} else {
					y1 = Math.min(y1, y - contactStep / 2);
					y2 = Math.max(y2, y + contactStep / 2);
				}
				if (eastFirst && northFirst && southFirst) {
					x2 = x - contactLength;
				}
				eastFirst = false;
				break;
			case SOUTH:
				if (southFirst && northFirst) {
					x1 = x - contactStep / 2;
					x2 = x + contactStep / 2;
				} else {
					x1 = Math.min(x1, x - contactStep / 2);
					x2 = Math.max(x2, x + contactStep / 2);
				}
				if (southFirst && westFirst && eastFirst) {
					y2 = y - contactLength;
				}
				southFirst = false;
				break;
			}
		}

		if (x1 > x2) {
			x1 = x2 = (x1 + x2) / 2;
		}
		if (y1 > y2) {
			y1 = y2 = (y1 + y2) / 2;
		}
		return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
	}

	private Rectangle2D getContactBestBox() {
		Rectangle2D minBox = getContactMinimalBox();
		double x1 = minBox.getMinX();
		double y1 = minBox.getMinY();
		double x2 = minBox.getMaxX();
		double y2 = minBox.getMaxY();

		boolean westFirst = true;
		boolean northFirst = true;
		boolean eastFirst = true;
		boolean southFirst = true;

		for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
			double x = vc.getBoundingBox().getCenterX();
			double y = vc.getBoundingBox().getCenterY();
			switch (vc.getDirection()) {
			case WEST:
				if (westFirst) {
					x1 = x + contactLength;
				} else {
					x1 = Math.max(x1, x + contactLength);
				}
				westFirst = false;
				break;
			case NORTH:
				if (northFirst) {
					y1 = y + contactLength;
				} else {
					y1 = Math.max(y1, y + contactLength);
				}
				northFirst = false;
				break;
			case EAST:
				if (eastFirst) {
					x2 = x - contactLength;
				} else {
					x2 = Math.min(x2, x - contactLength);
				}
				eastFirst = false;
				break;
			case SOUTH:
				if (southFirst) {
					y2 = y - contactLength;
				} else {
					y2 = Math.min(y2, y - contactLength);
				}
				southFirst = false;
				break;
			}
		}

		if (x1 > x2) {
			x1 = x2 = (x1 + x2) / 2;
		}
		if (y1 > y2) {
			y1 = y2 = (y1 + y2) / 2;
		}
		Rectangle2D maxBox = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
		return BoundingBoxHelper.union(minBox, maxBox);
	}

	private void drawContactLines(DrawRequest r) {
		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
			Line2D contactLine = null;
			switch (vc.getDirection()) {
			case NORTH:
				contactLine = new Line2D.Double(vc.getX(), vc.getY(), vc.getX(), bb.getMinY());
				break;
			case EAST:
				contactLine = new Line2D.Double(vc.getX(), vc.getY(), bb.getMaxX(), vc.getY());
				break;
			case SOUTH:
				contactLine = new Line2D.Double(vc.getX(), vc.getY(), vc.getX(), bb.getMaxY());
				break;
			case WEST:
				contactLine = new Line2D.Double(vc.getX(), vc.getY(), bb.getMinX(), vc.getY());
				break;
			}
			if (contactLine != null) {
				Graphics2D g = r.getGraphics();
				Decoration d = r.getDecoration();
				Color colorisation = d.getColorisation();
				g.setStroke(new BasicStroke((float) CircuitSettings.getWireWidth()));
				g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
				g.draw(contactLine);
			}
		}
	}

	private void drawContactLabel(DrawRequest r, VisualContact vc) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		Color colorisation = d.getColorisation();
		Color color = (vc.getIOType() == IOType.INPUT) ? inputColor : outputColor;
		g.setColor(Coloriser.colorise(color, colorisation));

		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		GlyphVector gv = vc.getNameGlyphs(r);
		Rectangle2D labelBB = gv.getVisualBounds();

		float labelX = 0.0f;
		float labelY = 0.0f;
		switch (vc.getDirection()) {
		case NORTH:
			labelX = (float) (-bb.getMinY() - marginSize - labelBB.getWidth());
			labelY = (float) (vc.getX() + labelBB.getHeight() / 2);
			break;
		case EAST:
			labelX = (float) (bb.getMaxX() - marginSize - labelBB.getWidth());
			labelY = (float) (vc.getY() + labelBB.getHeight() / 2);
			break;
		case SOUTH:
			labelX = (float) (-bb.getMaxY() + marginSize);
			labelY = (float) (vc.getX() + labelBB.getHeight() / 2);
			break;
		case WEST:
			labelX = (float) (bb.getMinX() + marginSize);
			labelY = (float) (vc.getY() + labelBB.getHeight() / 2);
			break;
		}
		g.drawGlyphVector(gv, labelX, labelY);
	}

	protected void drawContactLabels(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		AffineTransform oldTransform = g.getTransform();

		for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class,
				new Func<VisualContact, Boolean>() {
					@Override
					public Boolean eval(VisualContact arg) {
						return ((arg.getDirection() == Direction.WEST) || (arg.getDirection() == Direction.EAST));
					}
				})) {
			drawContactLabel(r, vc);
		}

		AffineTransform at = new AffineTransform();
		at.quadrantRotate(-1);
		g.transform(at);

		for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class,
				new Func<VisualContact, Boolean>() {
					@Override
					public Boolean eval(VisualContact arg) {
						return ((arg.getDirection() == Direction.NORTH) || (arg.getDirection() == Direction.SOUTH));
					}
				})) {
			drawContactLabel(r, vc);
		}

		g.setTransform(oldTransform);
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();

		// Cache rendered text to better estimate the bounding box
		cacheRenderedText(r);

		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		g.fill(bb);
		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		if (!getIsEnvironment()) {
			g.setStroke(new BasicStroke((float) CircuitSettings.getBorderWidth()));
		} else {
			float dash[] = { 0.25f, 0.25f };
			g.setStroke(new BasicStroke((float) CircuitSettings.getBorderWidth(),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f));
		}
		g.draw(bb);

		if (d.getColorisation() != null) {
			drawPivot(r);
		}

		drawContactLines(r);
		drawContactLabels(r);
		drawLabelInLocalSpace(r);
		drawNameInLocalSpace(r);
	}

	@Override
	public Rectangle2D getInternalBoundingBoxInLocalSpace() {
		if ((groupImpl != null) && (internalBB == null)) {
			Rectangle2D bb = getContactBestBox();
			double dx = Math.max(0.0, size - bb.getWidth());
			double dy = Math.max(0.0, size - bb.getHeight());
			internalBB = BoundingBoxHelper.expand(bb, dx, dy);
		}
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
		for (Node node : getChildren()) {
			if (node instanceof VisualNode) {
				VisualNode vn = (VisualNode)node;
				if (vn.hitTest(pointInLocalSpace)) {
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
			if ((x < bb.getMinX()) && (y > bb.getMinY()) && (y < bb.getMaxY())) {
				vc.setDirection(Direction.WEST);
			}
			if ((x > bb.getMaxX()) && (y > bb.getMinY()) && (y < bb.getMaxY())) {
				vc.setDirection(Direction.EAST);
			}
			if ((y < bb.getMinY()) && (x > bb.getMinX()) && (x < bb.getMaxX())) {
				vc.setDirection(Direction.NORTH);
			}
			if ((y > bb.getMaxY()) && (x > bb.getMinX()) && (x < bb.getMaxX())) {
				vc.setDirection(Direction.SOUTH);
			}
			invalidateBoundingBox();
		}

		if (e instanceof PropertyChangedEvent) {
			PropertyChangedEvent pc = (PropertyChangedEvent) e;
			String propertyName = pc.getPropertyName();
			if (propertyName.equals("name")
				|| propertyName.equals("IOtype")
				|| propertyName.equals("direction")
				|| propertyName.equals("setFunction")
				|| propertyName.equals("resetFunction")) {

				for (Node node : getChildren()) {
					if (node instanceof VisualFunctionContact) {
						VisualFunctionContact vc = (VisualFunctionContact) node;
						vc.invalidateRenderedFormula();
						vc.invalidateNameGlyph();
					}
				}
				invalidateBoundingBox();
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
