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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.util.Hierarchy;


public class VisualGroup extends VisualTransformableNode implements Drawable, Container,
ObservableHierarchy, Colorisable {
	public static final int HIT_COMPONENT = 1;
	public static final int HIT_CONNECTION = 2;
	public static final int HIT_GROUP = 3;

	DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

	public void draw(Graphics2D g) {
		Rectangle2D bb = getBoundingBoxInLocalSpace();

		if (bb != null && getParent() != null) {
			bb.setRect(bb.getX() - 0.1, bb.getY() - 0.1, bb.getWidth() + 0.2, bb.getHeight() + 0.2);
			g.setColor(Coloriser.colorise(Color.GRAY, getColorisation()));
			g.setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{0.2f, 0.2f}, 0.0f));
			g.draw(bb);
		}
	}

	@Override
	public void clearColorisation() {
		setColorisation(null);
		for (Colorisable node : Hierarchy.getChildrenOfType(this, Colorisable.class))
			node.clearColorisation();
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return BoundingBoxHelper.mergeBoundingBoxes(Hierarchy.getChildrenOfType(this, Touchable.class));
	}

	public final Collection<VisualComponent> getComponents() {
		return Hierarchy.getChildrenOfType(this, VisualComponent.class);
	}

	public final Collection<VisualConnection> getConnections() {
		return Hierarchy.getChildrenOfType(this, VisualConnection.class);
	}

	public void setColorisation(Color color) {
		super.setColorisation(color);
		for (Colorisable node : Hierarchy.getChildrenOfType(this, Colorisable.class))
			node.setColorisation(color);
	}

	public List<Node> unGroup() {
		ArrayList<Node> nodesToReparent = new ArrayList<Node>(groupImpl.getChildren());

		Container newParent = Hierarchy.getNearestAncestor(getParent(), Container.class);

		groupImpl.reparent(nodesToReparent, newParent);

		for (Node node : nodesToReparent)
			TransformHelper.applyTransform(node, localToParentTransform);

		return nodesToReparent;
	}

	public Set<MathNode> getMathReferences() {
		return Collections.emptySet();
	}
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return false;
	}


	public void add(Node node) {
		groupImpl.add(node);
	}


	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
	}


	public Collection<Node> getChildren() {
		return groupImpl.getChildren();
	}


	public Node getParent() {
		return groupImpl.getParent();
	}


	public void remove(Node node) {
		groupImpl.remove(node);
	}


	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}


	public void setParent(Node parent) {
		groupImpl.setParent(parent);
	}


	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}


	public void remove(Collection<Node> nodes) {
		groupImpl.remove(nodes);
	}


	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}


	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}
}