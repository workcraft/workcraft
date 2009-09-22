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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

public class HitMan
{
	private static <T extends Node> Iterable<T> filterByBB(Iterable<T> nodes, final Point2D pointInLocalSpace) {
		return
		Filter.filter(nodes, new UnaryFunctor<T, Boolean>()
				{
			private static final long serialVersionUID = -7790168871113424836L;

			@Override
			public Boolean fn(T arg) {
				if(!(arg instanceof Touchable))
					return true;

				Rectangle2D boundingBox = ((Touchable)arg).getBoundingBox();

				return
				boundingBox != null &&
				boundingBox.contains(pointInLocalSpace);
			}
				}
		);
	}

	private static Iterable<Node> getFilteredChildren(Point2D point, Node node)
	{
		return reverse(filterByBB(node.getChildren(), point));
	}

	public static Node hitDeepest(Point2D point, Node node, UnaryFunctor<Node, Boolean> filter) {
		Point2D transformedPoint = transformToChildSpace(point, node);

		for (Node n : getFilteredChildren(transformedPoint, node)) {
			Node result = hitDeepest(transformedPoint, n, filter);
			if(result!=null)
				return result;
		}

		if (filter.fn(node) && hitBranch (point, node))
			return node;

		return null;
	}

	public static boolean hitBranch (Point2D point, Node node) {

		if (node instanceof Touchable && ((Touchable)node).hitTest(point))	{
			if (node instanceof Hidable)
				return !((Hidable)node).isHidden();
			else
				return true;
		}

		Point2D transformedPoint = transformToChildSpace(point, node);

		for (Node n : getFilteredChildren(transformedPoint, node)) {
			if (hitBranch(transformedPoint, n))
				return true;
		}

		return false;
	}

	@SuppressWarnings("serial")
	public static Node hitFirst(Point2D point, Node node) {
		return hitFirst(point, node, new UnaryFunctor<Node, Boolean>(){
			public Boolean fn(Node arg0) {
				return true;
			}
		});
	}

	public static Node hitFirst(Point2D point, Node node, UnaryFunctor<Node, Boolean> filter) {
		if (filter.fn(node)) {
			if (hitBranch(point, node))
				return node;
			else
				return null;
		} else {
			return hitFirstChild(point, node, filter);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> T hitFirstNodeOfType(Point2D point, Node node, Class<T> type) {
		return (T) hitFirst(point, node, Hierarchy.getTypeFilter(type));
	}

	public static Node hitFirstChild(Point2D point,
			Node node, UnaryFunctor<Node, Boolean> filter) {
		Point2D transformedPoint = transformToChildSpace(point, node);
		for (Node n : getFilteredChildren(transformedPoint, node)) {
			Node hit = hitFirst(transformedPoint, n, filter);
			if (hit != null)
				return hit;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> T hitFirstChildOfType(Point2D point, Node node, Class<T> type) {
		return (T) hitFirstChild(point, node, Hierarchy.getTypeFilter(type));
	}

	private static Point2D transformToChildSpace(Point2D point,
			Node node) {
		Point2D transformedPoint;

		if (node instanceof Movable) {
			transformedPoint = new Point2D.Double();
			AffineTransform t = Geometry.optimisticInverse(((Movable)node).getTransform());
			t.transform(point, transformedPoint);
		} else
			transformedPoint = point;
		return transformedPoint;
	}


	private static <T> Iterable<T> reverse(Iterable<T> original)
	{
		final ArrayList<T> list = new ArrayList<T>();
		for (T node : original)
			list.add(node);
		return new Iterable<T>()
		{
			public Iterator<T> iterator() {
				return new Iterator<T>()
				{
					private int cur = list.size();
					public boolean hasNext() {
						return cur>0;
					}
					public T next() {
						return list.get(--cur);
					}
					public void remove() {
						throw new RuntimeException("Not supported");
					}
				};
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> T hitDeepestNodeOfType(Point2D point, Node group, final Class<T> type) {

		return (T)hitDeepest(point, group, Hierarchy.getTypeFilter(type));
	}


	@SuppressWarnings("serial")
	public static Node hitTestForSelection(Point2D point, Node node) {
		Node nd = HitMan.hitFirstChild(point, node, new UnaryFunctor<Node, Boolean>() {
			public Boolean fn(Node n) {
				if (!(n instanceof Movable))
					return false;

				if (n instanceof Hidable)
					return !((Hidable)n).isHidden();
				else
					return false;
			}
		});

		if (nd == null)
			nd = HitMan.hitFirstChild(point, node, new UnaryFunctor<Node, Boolean>() {
				public Boolean fn(Node n) {
					if (n instanceof VisualConnection) {
						if (n instanceof Hidable)
							return !((Hidable)n).isHidden();
						else
							return true;
					}
					else
						return false;
				}
			});

		return nd;
	}

	@SuppressWarnings("serial")
	public static Node hitTestForConnection(Point2D point, Node node) {
		Node nd = HitMan.hitDeepest(point, node, new UnaryFunctor<Node, Boolean>() {
			public Boolean fn(Node n) {
				if (n instanceof Movable && ! (n instanceof Container)) {
					if (n instanceof Hidable)
						return !((Hidable)n).isHidden();
					else
						return true;
				}
				else
					return false;
			}
		});

		if (nd == null)
			nd = HitMan.hitDeepest(point, node, new UnaryFunctor<Node, Boolean>() {
				public Boolean fn(Node n) {
					if (n instanceof VisualConnection) {
						if (n instanceof Hidable)
							return !((Hidable)n).isHidden();
						else
							return true;
					}
					else
						return false;
				}
			});

		return nd;
	}

	public static Node hitTestForConnection (Point2D point, VisualModel model) {
		AffineTransform t = TransformHelper.getTransform(model.getRoot(), model.getCurrentLevel());
		Point2D pt = new Point2D.Double();
		t.transform(point, pt);
		return hitTestForConnection(pt, model.getCurrentLevel());
	}

	public static Node hitTestForSelection (Point2D point, VisualModel model) {
		AffineTransform t = TransformHelper.getTransform(model.getRoot(), model.getCurrentLevel());
		Point2D pt = new Point2D.Double();
		t.transform(point, pt);
		return hitTestForSelection(pt, model.getCurrentLevel());
	}

	public static Collection<Node> boxHitTest (VisualGroup group, Point2D p1, Point2D p2) {
		Point2D p1local = new Point2D.Double();
		Point2D p2local = new Point2D.Double();
		group.getParentToLocalTransform().transform(p1, p1local);
		group.getParentToLocalTransform().transform(p2, p2local);

		LinkedList<Node> hit = new LinkedList<Node>();

		Rectangle2D rect = new Rectangle2D.Double(
				Math.min(p1local.getX(), p2local.getX()),
				Math.min(p1local.getY(), p2local.getY()),
				Math.abs(p1local.getX()-p2local.getX()),
				Math.abs(p1local.getY()-p2local.getY()));

		for (Touchable n : Hierarchy.getChildrenOfType(group, Touchable.class)) {
			if (n instanceof Hidable && ((Hidable)n).isHidden() )
				continue;

			if (p1local.getX()<=p2local.getX()) {
				if (TouchableHelper.insideRectangle(n, rect))
					hit.add((VisualNode)n);
			} else {
				if (TouchableHelper.touchesRectangle(n, rect))
					hit.add((VisualNode)n);
			}
		}
		return hit;
	}
}