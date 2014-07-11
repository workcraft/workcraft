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

package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.util.Hierarchy;


public class VisualGroup extends VisualTransformableNode implements Drawable, Collapsible, Container, ObservableHierarchy {
	public static final int HIT_COMPONENT = 1;
	public static final int HIT_CONNECTION = 2;
	public static final int HIT_GROUP = 3;

	private boolean isCollapsed = false;
	@Override
	public void setIsCollapsed(boolean isCollapsed) {
		sendNotification(new TransformChangingEvent(this));

		this.isCollapsed = isCollapsed;
		Point2D newCentre = AbstractVisualModel.centralizeComponents(getChildren());
		this.setPosition(new Point2D.Double(this.getPosition().getX() + newCentre.getX(), this.getPosition().getY() + newCentre.getY()));

		sendNotification(new TransformChangedEvent(this));
	}

	@Override
	public boolean getIsCollapsed() {
		return isCollapsed;
	}

	private boolean isInside = false;
	@Override
	public void setIsCurrentLevelInside(boolean isInside) {
		sendNotification(new TransformChangingEvent(this));
		this.isInside = isInside;
		sendNotification(new TransformChangedEvent(this));
	}

	public boolean isCurrentLevelInside() {
		return isInside;
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualGroup, Boolean>(
				this, "Is collapsed", Boolean.class) {

			@Override
			protected void setter(VisualGroup object, Boolean value) {
				object.setIsCollapsed(value);
			}
			@Override
			protected Boolean getter(VisualGroup object) {
				return object.getIsCollapsed();
			}
		});
	}

	public VisualGroup() {
		super();
		addPropertyDeclarations();
	}


	DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

	@Override
	public void draw(DrawRequest r) {

		// This is to update the rendered text for names (and labels) of group children,
		// which is necessary to calculate the bounding box before children have been drawn
		for (VisualComponent component: Hierarchy.getChildrenOfType(this, VisualComponent.class)) {
			component.cacheRenderedText(r);
		}

		Rectangle2D bb = getBoundingBoxInLocalSpace();

		if (bb != null && getParent() != null) {


			if (getIsCollapsed()&&!isCurrentLevelInside()) {
				bb.setRect(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight());
				Graphics2D g = r.getGraphics();


				g.setColor(Coloriser.colorise(Color.BLACK, r.getDecoration().getColorisation()));
				float[] pattern = {0.2f, 0.2f};
				g.setStroke(new BasicStroke(0.05f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));
				g.draw(bb);

			} else {

				bb.setRect(bb.getX() - 0.1, bb.getY() - 0.1, bb.getWidth() + 0.2, bb.getHeight() + 0.2);
				Graphics2D g = r.getGraphics();

				g.setColor(Coloriser.colorise(Color.GRAY, r.getDecoration().getColorisation()));
				float[] pattern = {0.2f, 0.2f};
				g.setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));
				g.draw(bb);

			}

//			// draw the collapse button
//			Rectangle2D sb = new Rectangle2D.Double();
//			sb.setRect(bb.getMaxX()-0.5, bb.getMinY(), 0.5, 0.5);
//			g.setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, null, 0.0f));
//			g.draw(sb);
		}
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		if (getIsCollapsed()&&!isCurrentLevelInside()) {
			Rectangle2D sb = new Rectangle2D.Double();
			sb.setRect(-0.5, -0.5, 1, 1);
			return sb;
		} else
			return BoundingBoxHelper.mergeBoundingBoxes(Hierarchy.getChildrenOfType(this, Touchable.class));
	}

	public final Collection<VisualComponent> getComponents() {
		return Hierarchy.getChildrenOfType(this, VisualComponent.class);
	}

	public final Collection<VisualConnection> getConnections() {
		return Hierarchy.getChildrenOfType(this, VisualConnection.class);
	}

	public List<Node> unGroup() {
		ArrayList<Node> nodesToReparent = new ArrayList<Node>(groupImpl.getChildren());

		Container newParent = Hierarchy.getNearestAncestor(getParent(), Container.class);

		groupImpl.reparent(nodesToReparent, newParent);

		for (Node node : nodesToReparent)
			TransformHelper.applyTransform(node, localToParentTransform);

		return nodesToReparent;
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		Rectangle2D bb = getBoundingBoxInLocalSpace();

		if (bb != null && getParent() != null) {

			if (getIsCollapsed()&&!isCurrentLevelInside()) return bb.contains(pointInLocalSpace);

			Rectangle2D sb = new Rectangle2D.Double();

			sb.setRect(bb.getMaxX()-0.5, bb.getMinY(), 0.5, 0.5);
			return sb.contains(pointInLocalSpace);
		}

		return false;
	}

	@Override
	public void add(Node node) {
		groupImpl.add(node);
	}

	@Override
	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
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
	public void remove(Node node) {
		groupImpl.remove(node);
	}


	@Override
	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}


	@Override
	public void setParent(Node parent) {
		groupImpl.setParent(parent);
	}


	@Override
	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}


	@Override
	public void remove(Collection<Node> nodes) {
		groupImpl.remove(nodes);
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
	public Point2D getCenterInLocalSpace() {
		Rectangle2D bb = getBoundingBoxInLocalSpace();
		if (bb != null)
			return new Point2D.Double(bb.getCenterX(), bb.getCenterY());

		return new Point2D.Double(0, 0);
	}

}