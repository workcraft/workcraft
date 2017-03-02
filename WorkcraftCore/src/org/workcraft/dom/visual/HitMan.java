package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.util.Func;
import org.workcraft.util.Func2;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;

public class HitMan {

    @SuppressWarnings("serial")
    private static <T extends Node> Iterable<T> filterByBB(Iterable<T> nodes, final Point2D pointInLocalSpace) {
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

    private static Iterable<Node> getFilteredChildren(Point2D point, Node node) {
        return reverse(filterByBB(node.getChildren(), point));
    }

    private static <T> Iterable<T> reverse(Iterable<T> original) {
        final ArrayList<T> list = new ArrayList<>();
        for (T node : original) {
            list.add(node);
        }
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private int cur = list.size();
                    public boolean hasNext() {
                        return cur > 0;
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

    public static Node hitDeepest(Point2D point, Node node, final Func<Node, Boolean> filter) {
        return hitDeepest(point, node, new Func2<Point2D, Node, Boolean>() {
            @Override
            public Boolean eval(Point2D point, Node node) {
                return filter.eval(node);
            }
        });
    }

    public static Node hitDeepest(Point2D point, Node node, final Func2<Point2D, Node, Boolean> filter) {
        Point2D transformedPoint = transformToChildSpace(point, node);
        for (Node n : getFilteredChildren(transformedPoint, node)) {
            Node result = hitDeepest(transformedPoint, n, filter);
            if (result != null) {
                return result;
            }
        }
        return filter.eval(point, node) ? hitBranch(point, node) : null;
    }

    public static boolean isBranchHit(Point2D point, Node node) {
        if (node instanceof Touchable && ((Touchable) node).hitTest(point)) {
            if (node instanceof Hidable) {
                return !((Hidable) node).isHidden();
            } else {
                return true;
            }
        }
        Point2D transformedPoint = transformToChildSpace(point, node);
        for (Node n : getFilteredChildren(transformedPoint, node)) {
            if (isBranchHit(transformedPoint, n)) {
                return true;
            }
        }
        return false;
    }

    public static Node hitFirst(Point2D point, Node node) {
        return hitFirst(point, node, new Func<Node, Boolean>() {
            public Boolean eval(Node arg0) {
                return true;
            }
        });
    }

    public static Node hitFirst(Point2D point, Node node, Func<Node, Boolean> filter) {
        if (filter.eval(node)) {
            return hitBranch(point, node);
        } else {
            return hitFirstChild(point, node, filter);
        }
    }

    private static Node hitBranch(Point2D point, Node node) {
        if (node instanceof CustomTouchable) {
            return ((CustomTouchable) node).hitCustom(point);
        }
        return isBranchHit(point, node) ? node : null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> T hitFirstNodeOfType(Point2D point, Node node, Class<T> type) {
        return (T) hitFirst(point, node, Hierarchy.getTypeFilter(type));
    }

    public static Node hitFirstChild(Point2D point, Node node, Func<Node, Boolean> filter) {
        Point2D transformedPoint = transformToChildSpace(point, node);
        for (Node n : getFilteredChildren(transformedPoint, node)) {
            Node hit = hitFirst(transformedPoint, n, filter);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> T hitFirstChildOfType(Point2D point, Node node, Class<T> type) {
        return (T) hitFirstChild(point, node, Hierarchy.getTypeFilter(type));
    }

    private static Point2D transformToChildSpace(Point2D point, Node node) {
        Point2D transformedPoint;
        if (node instanceof Movable) {
            transformedPoint = new Point2D.Double();
            AffineTransform at = Geometry.optimisticInverse(((Movable) node).getTransform());
            at.transform(point, transformedPoint);
        } else {
            transformedPoint = point;
        }
        return transformedPoint;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> T hitDeepestNodeOfType(Point2D point, Node node, final Class<T> type) {
        return (T) hitDeepest(point, node, Hierarchy.getTypeFilter(type));
    }

    public static Node hitTestRootDeepest(Point2D point, VisualModel model) {
        Point2D pt = transformToChildSpace(point, model.getRoot());
        return hitTestDeepest(pt, model.getRoot());
    }

    public static Node hitTestCurrentLevelFirst(Point2D point, VisualModel model) {
        AffineTransform at = TransformHelper.getTransform(model.getRoot(), model.getCurrentLevel());
        Point2D pt = at.transform(point, null);
        return hitTestFirst(pt, model.getCurrentLevel());
    }

    public static Node hitTestFirst(Point2D point, Node node) {
        Node result = null;
        if (result == null) {
            result = hitTestFirstMovable(point, node);
        }
        if (result == null) {
            result = hitTestFirstConnection(point, node);
        }
        return result;
    }

    private static Node hitTestFirstConnection(Point2D point, Node node) {
        return HitMan.hitFirstChild(point, node, new Func<Node, Boolean>() {
            public Boolean eval(Node n) {
                boolean isConnection = n instanceof VisualConnection;
                boolean isShown = !((n instanceof Hidable) && ((Hidable) n).isHidden());
                return isConnection && isShown;
            }
        });
    }

    private static Node hitTestFirstMovable(Point2D point, Node node) {
        return HitMan.hitFirstChild(point, node, new Func<Node, Boolean>() {
            public Boolean eval(Node n) {
                boolean isMovable = n instanceof Movable;
                boolean isShown = !((n instanceof Hidable) && ((Hidable) n).isHidden());
                return isMovable && isShown;
            }
        });
    }

    public static Node hitTestCurrentLevelDeepest(Point2D point, VisualModel model) {
        AffineTransform at = TransformHelper.getTransform(model.getRoot(), model.getCurrentLevel());
        Point2D pt = at.transform(point, null);
        return hitTestDeepest(pt, model.getCurrentLevel());
    }

    public static Node hitTestDeepest(Point2D point, Node node) {
        Node result = null;
        if (result == null) {
            result = hitTestDeepestMovable(point, node);
        }
        if (result == null) {
            result = hitTestDeepestConnection(point, node);
        }
        return result;
    }

    private static Node hitTestDeepestMovable(Point2D point, Node node) {
        return HitMan.hitDeepest(point, node, new Func<Node, Boolean>() {
            public Boolean eval(Node n) {
                boolean isMovable = n instanceof Movable;
                boolean isShown = !((n instanceof Hidable) && ((Hidable) n).isHidden());
                return isMovable && isShown;
            }
        });
    }

    private static Node hitTestDeepestConnection(Point2D point, Node node) {
        return HitMan.hitDeepest(point, node, new Func<Node, Boolean>() {
            public Boolean eval(Node n) {
                boolean isConnection = n instanceof VisualConnection;
                boolean isShown = !((n instanceof Hidable) && ((Hidable) n).isHidden());
                return isConnection && isShown;
            }
        });
    }

    /**
     * The method finds all direct children of the given container, which completely fit inside the given rectangle.
     * @param container The container whose children will be examined
     * @param p1         The top-left corner of the rectangle, in the parent coordinates for the container
     * @param p2         The bottom-right corner of the rectangle
     * @return             The collection of nodes fitting completely inside the rectangle
     */
    public static Collection<Node> boxHitTest(Container container, Point2D p1, Point2D p2) {

        if (container instanceof Movable) {
            AffineTransform toLocal = Geometry.optimisticInverse(((Movable) container).getTransform());
            toLocal.transform(p1, p1);
            toLocal.transform(p2, p2);
        }

        LinkedList<Node> hit = new LinkedList<>();

        Rectangle2D rect = new Rectangle2D.Double(
                Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()),
                Math.abs(p1.getX() - p2.getX()),
                Math.abs(p1.getY() - p2.getY()));

        for (Touchable n : Hierarchy.getChildrenOfType(container, Touchable.class)) {
            if (n instanceof Hidable && ((Hidable) n).isHidden()) {
                continue;
            }

            if (p1.getX() <= p2.getX()) {
                if (TouchableHelper.insideRectangle(n, rect)) {
                    hit.add((Node) n);
                }
            } else {
                if (TouchableHelper.touchesRectangle(n, rect)) {
                    hit.add((Node) n);
                }
            }
        }
        return hit;
    }
}
