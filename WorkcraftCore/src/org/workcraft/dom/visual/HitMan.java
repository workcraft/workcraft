package org.workcraft.dom.visual;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.util.Func;
import org.workcraft.util.Func2;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class HitMan {

    /**
     * The method finds all direct children of the given container, which completely fit inside the given rectangle.
     * @param container The container whose children will be examined
     * @param p1  The top-left corner of the rectangle, in the parent coordinates for the container
     * @param p2  The bottom-right corner of the rectangle
     * @return    The collection of nodes fitting completely inside the rectangle
     */
    public static Collection<VisualNode> hitBox(Container container, Point2D p1, Point2D p2) {
        if (container instanceof Movable) {
            AffineTransform toLocal = Geometry.optimisticInverse(((Movable) container).getTransform());
            toLocal.transform(p1, p1);
            toLocal.transform(p2, p2);
        }
        LinkedList<VisualNode> result = new LinkedList<>();
        Rectangle2D rect = new Rectangle2D.Double(
                Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()),
                Math.abs(p1.getX() - p2.getX()),
                Math.abs(p1.getY() - p2.getY()));

        for (VisualNode node: Hierarchy.getChildrenOfType(container, VisualNode.class)) {
            if (node.isHidden()) continue;
            if (p1.getX() <= p2.getX()) {
                if (TouchableHelper.insideRectangle(node, rect)) {
                    result.add(node);
                }
            } else {
                if (TouchableHelper.touchesRectangle(node, rect)) {
                    result.add(node);
                }
            }
        }
        return result;
    }

    public static VisualNode hitFirstInCurrentLevel(Point2D point, VisualModel model) {
        Container currentLevel = model.getCurrentLevel();
        AffineTransform at = TransformHelper.getTransform(model.getRoot(), currentLevel);
        Point2D pointInLocalSpace = at.transform(point, null);
        return hitFirstChild(pointInLocalSpace, currentLevel);
    }

    public static VisualNode hitFirstChild(Point2D point, Container container) {
        VisualNode node = HitMan.hitFirstChild(point, container, VisualTransformableNode.class);
        // Top priority to connection control points
        if (node instanceof ControlPoint) {
            return node;
        }
        VisualConnection connection = HitMan.hitFirstChild(point, container, VisualConnection.class);
        // Try connections in the same container not touching the hit node
        if ((node == null) || (connection != null) && (connection.getParent() == container)
                && (connection.getFirst() != node) && (connection.getSecond() != node)) {
            return connection;
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> T hitFirstChild(Point2D point, Node node, final Class<T> type) {
        return (T) hitFirstChild(point, node, Hierarchy.getTypeFilter(type));
    }

    public static Node hitFirstChild(Point2D point, Node parentNode, Func<Node, Boolean> filter) {
        Node result = null;
        Point2D pointInLocalSpace = transformToChildSpace(point, parentNode);
        Iterable<Node> filteredChildren = getFilteredChildren(pointInLocalSpace, parentNode);
        for (Node childNode: filteredChildren) {
            if (filter.eval(childNode)) {
                Node branchNode = hitBranch(pointInLocalSpace, childNode);
                if (filter.eval(branchNode)) {
                    result = branchNode;
                }
            } else {
                result = hitFirstChild(pointInLocalSpace, childNode, filter);
            }
            if (result != null) break;
        }
        return result;
    }

    private static Node hitBranch(Point2D point, Node node) {
        if (node instanceof CustomTouchable) {
            return ((CustomTouchable) node).hitCustom(point);
        }
        return isBranchHit(point, node) ? node : null;
    }

    private static boolean isBranchHit(Point2D point, Node node) {
        if ((node instanceof Touchable) && ((Touchable) node).hitTest(point)) {
            if (node instanceof Hidable) {
                return !((Hidable) node).isHidden();
            } else {
                return true;
            }
        }
        Point2D pointInLocalSpace = transformToChildSpace(point, node);
        Iterable<Node> filteredChildren = getFilteredChildren(pointInLocalSpace, node);
        for (Node childNode: filteredChildren) {
            if (isBranchHit(pointInLocalSpace, childNode)) {
                return true;
            }
        }
        return false;
    }

    public static Node hitDeepest(Point2D point, VisualModel model) {
        Container root = model.getRoot();
        Point2D pointInLocalSpace = transformToChildSpace(point, root);
        return hitDeepest(pointInLocalSpace, root);
    }

    public static Node hitDeepest(Point2D point, Container container) {
        VisualTransformableNode vertex = HitMan.hitDeepest(point, container, VisualTransformableNode.class);
        if (vertex instanceof ControlPoint) {
            return vertex;
        }
        VisualConnection connection = HitMan.hitDeepest(point, container, VisualConnection.class);
        if (connection != null) {
            if ((connection.getFirst() != vertex) && (connection.getSecond() != vertex)) {
                return connection;
            }
        }
        return vertex;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> T hitDeepest(Point2D point, Node node, final Class<T> type) {
        return (T) hitDeepest(point, node, Hierarchy.getTypeFilter(type));
    }

    public static Node hitDeepest(Point2D point, Node node, final Func<Node, Boolean> filter) {
        return hitDeepest(point, node, (point1, node1) -> filter.eval(node1));
    }

    private static Node hitDeepest(Point2D point, Node node, final Func2<Point2D, Node, Boolean> filter) {
        Point2D pointInLocalSpace = transformToChildSpace(point, node);
        Iterable<Node> filteredChildren = getFilteredChildren(pointInLocalSpace, node);
        for (Node childNode: filteredChildren) {
            Node result = hitDeepest(pointInLocalSpace, childNode, filter);
            if (result != null) {
                return result;
            }
        }
        return filter.eval(point, node) ? hitBranch(point, node) : null;
    }

    private static Point2D transformToChildSpace(Point2D point, Node node) {
        if (node instanceof Movable) {
            Movable movable = (Movable) node;
            AffineTransform at = Geometry.optimisticInverse(movable.getTransform());
            return at.transform(point, null);
        }
        return point;
    }

    private static Iterable<Node> getFilteredChildren(Point2D pointInLocalSpace, Node node) {
        Collection<Node> children = node.getChildren();
        Iterable<Node> filterByBoundingBox = filterByBoundingBox(pointInLocalSpace, children);
        return reverse(filterByBoundingBox);
    }

    @SuppressWarnings("serial")
    private static <T extends Node> Iterable<T> filterByBoundingBox(final Point2D pointInLocalSpace, Iterable<T> nodes) {
        return Filter.filter(nodes, new UnaryFunctor<T, Boolean>() {
            @Override
            public Boolean fn(T arg) {
                if (!(arg instanceof Touchable)) {
                    return true;
                }
                Rectangle2D boundingBox = ((Touchable) arg).getBoundingBox();
                return (boundingBox != null) && boundingBox.contains(pointInLocalSpace);
            }
        });
    }

    private static <T> Iterable<T> reverse(Iterable<T> original) {
        final ArrayList<T> list = new ArrayList<>();
        for (T node : original) {
            list.add(node);
        }
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private int cur = list.size();
                    @Override
                    public boolean hasNext() {
                        return cur > 0;
                    }
                    @Override
                    public T next() {
                        return list.get(--cur);
                    }
                    @Override
                    public void remove() {
                        throw new RuntimeException("Not supported");
                    }
                };
            }
        };
    }

}
