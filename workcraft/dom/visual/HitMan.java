package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;

import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

public class HitMan
{
	private static <T extends HierarchyNode> Iterable<T> filterByBB(Iterable<T> nodes, final Point2D pointInLocalSpace) {
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

	private static Iterable<HierarchyNode> getFilteredChildren(Point2D point, HierarchyNode node)
	{
		return reverse(filterByBB(node.getChildren(), point));
	}

	public static HierarchyNode hitDeepest(Point2D point, HierarchyNode node, UnaryFunctor<HierarchyNode, Boolean> filter) {
		Point2D transformedPoint = transformToChildSpace(point, node);

		for (HierarchyNode n : getFilteredChildren(transformedPoint, node)) {
			HierarchyNode result = hitDeepest(transformedPoint, n, filter);
			return result;
		}

		if (filter.fn(node) && hitBranch (point, node))
			return node;

		return null;
	}

	public static boolean hitBranch (Point2D point, HierarchyNode node) {

		if (node instanceof Touchable && ((Touchable)node).hitTest(point))
			return true;

		Point2D transformedPoint = transformToChildSpace(point, node);

		for (HierarchyNode n : getFilteredChildren(transformedPoint, node)) {
			if (hitBranch(transformedPoint, n))
				return true;
		}

		return false;
	}

	@SuppressWarnings("serial")
	public static HierarchyNode hitFirst(Point2D point, HierarchyNode node) {
		return hitFirst(point, node, new UnaryFunctor<HierarchyNode, Boolean>(){
			public Boolean fn(HierarchyNode arg0) {
				return true; // its a kind of magic
			}
		});
	}

	public static HierarchyNode hitFirst(Point2D point, HierarchyNode node, UnaryFunctor<HierarchyNode, Boolean> filter) {
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
	public static <T extends HierarchyNode> T hitFirstNodeOfType(Point2D point, HierarchyNode node, Class<T> type) {
		return (T) hitFirst(point, node, Hierarchy.getTypeFilter(type));
	}

	public static HierarchyNode hitFirstChild(Point2D point,
			HierarchyNode node, UnaryFunctor<HierarchyNode, Boolean> filter) {
		Point2D transformedPoint = transformToChildSpace(point, node);
		for (HierarchyNode n : getFilteredChildren(transformedPoint, node)) {
			HierarchyNode hit = hitFirst(transformedPoint, n, filter);
			if (hit != null)
				return hit;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends HierarchyNode> T hitFirstChildOfType(Point2D point, HierarchyNode node, Class<T> type) {
		return (T) hitFirstChild(point, node, Hierarchy.getTypeFilter(type));
	}

	private static Point2D transformToChildSpace(Point2D point,
			HierarchyNode node) {
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
	public static <T extends HierarchyNode> T hitDeepestNodeOfType(Point2D point, VisualGroup group, final Class<T> type) {
		return (T)hitDeepest(point, group, Hierarchy.getTypeFilter(type));
	}


	@SuppressWarnings("serial")
	public static HierarchyNode hitTestForSelection(Point2D point, HierarchyNode node) {
		HierarchyNode nd = HitMan.hitFirstChild(point, node, new UnaryFunctor<HierarchyNode, Boolean>() {
			public Boolean fn(HierarchyNode n) {
				if (n instanceof Movable)
					return true;
				else
					return false;
			}
		});

		if (nd == null)
			nd = HitMan.hitFirstChild(point, node, new UnaryFunctor<HierarchyNode, Boolean>() {
				public Boolean fn(HierarchyNode n) {
					if (n instanceof VisualConnection)
						return true;
					else
						return false;
				}
			});

		return nd;
	}
}