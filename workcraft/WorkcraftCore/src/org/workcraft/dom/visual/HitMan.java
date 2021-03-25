package org.workcraft.dom.visual;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.types.Func;
import org.workcraft.types.Func2;
import org.workcraft.utils.Geometry;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

        for (VisualNode node : Hierarchy.getChildrenOfType(container, VisualNode.class)) {
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
        return (T) hitFirstChild(point, node, type::isInstance);
    }

    public static Node hitFirstChild(Point2D point, Node parentNode, Func<Node, Boolean> filter) {
        Node result = null;
        Point2D pointInLocalSpace = transformToChildSpace(point, parentNode);
        for (Node childNode : getHitableChildrenInReverseOrder(parentNode)) {
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
        for (Node childNode : getHitableChildrenInReverseOrder(node)) {
            if (isBranchHit(pointInLocalSpace, childNode)) {
                return true;
            }
        }
        return false;
    }

    public static VisualNode hitDeepest(Point2D point, VisualModel model) {
        Container root = model.getRoot();
        Point2D pointInLocalSpace = transformToChildSpace(point, root);
        return hitDeepest(pointInLocalSpace, root);
    }

    public static VisualNode hitDeepest(Point2D point, Container container) {
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
        return (T) hitDeepest(point, node, type::isInstance);
    }

    public static Node hitDeepest(Point2D point, Node node, final Func<Node, Boolean> filter) {
        return hitDeepest(point, node, (point1, node1) -> filter.eval(node1));
    }

    private static Node hitDeepest(Point2D point, Node node, final Func2<Point2D, Node, Boolean> filter) {
        Point2D pointInLocalSpace = transformToChildSpace(point, node);
        //Iterable<Node> filteredChildren = getFilteredChildren(pointInLocalSpace, node);
        for (Node childNode : node.getChildren()) {
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

    private static Collection<Node> getHitableChildrenInReverseOrder(Node parentNode) {
        if (parentNode instanceof Collapsible) {
            Collapsible collapsible = (Collapsible) parentNode;
            if (collapsible.getIsCollapsed() && !collapsible.isCurrentLevelInside()) {
                return Collections.EMPTY_LIST;
            }
        }
        final ArrayList<Node> result = new ArrayList<>(parentNode.getChildren());
        Collections.reverse(result);
        return result;
    }

}
