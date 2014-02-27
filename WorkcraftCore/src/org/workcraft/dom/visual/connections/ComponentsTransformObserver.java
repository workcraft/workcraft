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

package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Collapsible;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformDispatcher;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.observation.TransformEvent;
import org.workcraft.observation.TransformObserver;

public class ComponentsTransformObserver implements TransformObserver, Node {
	private Point2D firstCenter = new Point2D.Double();
	private Point2D secondCenter = new Point2D.Double();

	private Touchable firstShape;
	private Touchable secondShape;

	private VisualConnection connection;

	private boolean valid = false;

	public ComponentsTransformObserver (VisualConnection connection) {
		this.connection = connection;
	}

	public Point2D getFirstCenter() {
		if (!valid)
			update();

		return (Point2D)firstCenter.clone();
	}

	public Point2D getSecondCenter() {
		if (!valid)
			update();

		return (Point2D)secondCenter.clone();
	}

	public Touchable getFirstShape() {
		if (!valid)
			update();

		return firstShape;
	}

	public Touchable getSecondShape() {
		if (!valid)
			update();

		return secondShape;
	}

	@Override
	public void notify(TransformEvent e) {
		if (e instanceof TransformChangingEvent) {
			connection.getGraphic().componentsTransformChanging();
		}
		else if (e instanceof TransformChangedEvent) {
			valid = false;
			connection.getGraphic().componentsTransformChanged();
		}
	}

	private void update() {


		VisualNode firstComponent = connection.getFirst();

		VisualNode cur = firstComponent;
		while (cur.getParent()!=null) {
			boolean isCollapsed = cur instanceof Collapsible&&((Collapsible)cur).getIsCollapsed()&&!((Collapsible)cur).isCurrentLevelInside();

			if (isCollapsed) {
				firstComponent = cur;
			}

			cur = (VisualNode)cur.getParent();
		}


		VisualNode secondComponent = connection.getSecond();
		cur = secondComponent;
		while (cur.getParent()!=null) {
			boolean isCollapsed = cur instanceof Collapsible&&((Collapsible)cur).getIsCollapsed()&&!((Collapsible)cur).isCurrentLevelInside();

			if (isCollapsed) {
				secondComponent = cur;
			}
			cur = (VisualNode)cur.getParent();
		}


		firstShape = TransformHelper.transform(firstComponent, TransformHelper.getTransform(firstComponent, connection));
		secondShape = TransformHelper.transform(secondComponent, TransformHelper.getTransform(secondComponent, connection));

		firstCenter = firstShape.getCenter();
		secondCenter = secondShape.getCenter();

		valid = true;
	}

	@Override
	public void subscribe(TransformDispatcher dispatcher) {
		dispatcher.subscribe(this, connection.getFirst());
		dispatcher.subscribe(this, connection.getSecond());
	}

	@Override
	public Collection<Node> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public Node getParent() {
		return connection;
	}

	@Override
	public void setParent(Node parent) {
		throw new RuntimeException ("Node does not support reparenting");
	}
}