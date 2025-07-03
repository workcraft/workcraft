package org.workcraft.dom.visual;

import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

public class VisualModelTransformer {

    private static void transformNodes(Collection<? extends VisualNode> nodes, AffineTransform t) {
        assert nodes != null;
        // Control points may move while the nodes are repositioned, therefore this two-step transformation.
        HashMap<ControlPoint, Point2D> controlPointPositions = new HashMap<>();
        for (VisualNode node: nodes) {
            if (node instanceof VisualConnection vc) {
                for (ControlPoint cp: vc.getGraphic().getControlPoints()) {
                    controlPointPositions.put(cp, cp.getPosition());
                }
            }
        }
        // First reposition vertices.
        for (VisualNode node: nodes) {
            if (node instanceof VisualTransformableNode vn) {
                Point2D pos = vn.getPosition();
                if ((node instanceof VisualGroup) || (node instanceof VisualPage)) {
                    AffineTransform t2 = new AffineTransform();
                    t2.translate(-pos.getX(), -pos.getY());
                    t2.concatenate(t);
                    t2.translate(pos.getX(), pos.getY());

                    AffineTransform t3 = AffineTransform.getTranslateInstance(-t2.getTranslateX(), -t2.getTranslateY());
                    t3.concatenate(t2);
                    Collection<VisualNode> children = NodeHelper.filterByType(vn.getChildren(), VisualNode.class);
                    transformNodes(children, t3);

                    t.transform(pos, pos);
                    vn.setPosition(pos);
                } else {
                    t.transform(pos, pos);
                    vn.setPosition(pos);
                }
            }
        }
        // Then reposition control points, using their stored initial positions.
        for (VisualNode node: nodes) {
            if (node instanceof VisualConnection vc) {
                for (ControlPoint cp: vc.getGraphic().getControlPoints()) {
                    Point2D pos = controlPointPositions.get(cp);
                    t.transform(pos, pos);
                    cp.setPosition(pos);
                }
            }
        }
    }

    public static void translateNodes(Collection<? extends VisualNode> nodes, double tx, double ty) {
        AffineTransform t = AffineTransform.getTranslateInstance(tx, ty);
        transformNodes(nodes, t);
    }

    public static void translateSelection(VisualModel vm, double tx, double ty) {
        translateNodes(vm.getSelection(), tx, ty);
    }

    public static void scaleSelection(VisualModel vm, double sx, double sy) {
        Rectangle2D selectionBB = getNodesCoordinateBox(vm.getSelection());
        if (selectionBB != null) {
            AffineTransform t = new AffineTransform();
            Point2D cp = new Point2D.Double(selectionBB.getCenterX(), selectionBB.getCenterY());
            t.translate(cp.getX(), cp.getY());
            t.scale(sx, sy);
            t.translate(-cp.getX(), -cp.getY());

            transformNodes(vm.getSelection(), t);
        }
    }

    public static void rotateSelection(VisualModel vm, double theta) {
        Rectangle2D selectionBB = getNodesCoordinateBox(vm.getSelection());
        if (selectionBB != null) {
            AffineTransform t = new AffineTransform();
            Point2D cp = new Point2D.Double(selectionBB.getCenterX(), selectionBB.getCenterY());

            t.translate(cp.getX(), cp.getY());
            t.rotate(theta);
            t.translate(-cp.getX(), -cp.getY());

            transformNodes(vm.getSelection(), t);
        }
    }

    private static Rectangle2D bbUnion(Rectangle2D bb1, Point2D bb2) {
        if (bb2 == null) {
            return bb1;
        }

        Rectangle2D r = new Rectangle2D.Double(bb2.getX(), bb2.getY(), 0, 0);
        if (bb1 == null) {
            bb1 = r;
        } else Rectangle2D.union(bb1, r, bb1);

        return bb1;
    }

    private static Rectangle2D getNodesCoordinateBox(Collection<? extends VisualNode> nodes) {
        Rectangle2D selectionBB = null;
        for (VisualNode vn: nodes) {
            if (vn instanceof VisualTransformableNode) {
                Point2D pos = ((VisualTransformableNode) vn).getPosition();
                selectionBB = bbUnion(selectionBB, pos);
            }
        }
        return selectionBB;
    }

    public static HashMap<VisualTransformableNode, Point2D> getRootSpacePositions(Collection<? extends VisualNode> nodes) {
        HashMap<VisualTransformableNode, Point2D> componentToPositionMap = new HashMap<>();
        for (VisualNode node: nodes) {
            if (node instanceof VisualTransformableNode component) {
                Point2D position = component.getRootSpacePosition();
                componentToPositionMap.put(component, position);
            } else if (node instanceof VisualConnection connection) {
                for (ControlPoint cp: connection.getGraphic().getControlPoints()) {
                    Point2D position = cp.getRootSpacePosition();
                    componentToPositionMap.put(cp, position);
                }
            }
        }
        return componentToPositionMap;
    }

    public static void setRootSpacePositions(HashMap<VisualTransformableNode, Point2D> componentToPositionMap) {
        if (componentToPositionMap != null) {
            // First reposition vertices.
            for (Entry<VisualTransformableNode, Point2D> entry: componentToPositionMap.entrySet()) {
                VisualTransformableNode vn = entry.getKey();
                if (!(vn instanceof ControlPoint)) {
                    Point2D position = entry.getValue();
                    vn.setRootSpacePosition(position);
                }
            }
            // Then reposition control points.
            for (Entry<VisualTransformableNode, Point2D> entry: componentToPositionMap.entrySet()) {
                VisualTransformableNode vn = entry.getKey();
                if (vn instanceof ControlPoint) {
                    Point2D position = entry.getValue();
                    vn.setRootSpacePosition(position);
                }
            }
        }
    }

}
