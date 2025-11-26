package org.workcraft.dom.visual.connections;

import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.utils.Geometry;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConnectionUtils {

    public static HashMap<VisualConnection, VisualConnection.ScaleMode> replaceConnectionScaleMode(
            Collection<VisualConnection> connections, VisualConnection.ScaleMode mode) {

        HashMap<VisualConnection, VisualConnection.ScaleMode> connectionToScaleModeMap = new HashMap<>();
        for (VisualConnection vc: connections) {
            connectionToScaleModeMap.put(vc, vc.getScaleMode());
            vc.setScaleMode(mode);
        }
        return connectionToScaleModeMap;
    }

    public static void restoreConnectionScaleMode(HashMap<VisualConnection, VisualConnection.ScaleMode> connectionToScaleModeMap) {
        if (connectionToScaleModeMap != null) {
            for (Map.Entry<VisualConnection, VisualConnection.ScaleMode> entry : connectionToScaleModeMap.entrySet()) {
                VisualConnection vc = entry.getKey();
                VisualConnection.ScaleMode scaleMode = entry.getValue();
                vc.setScaleMode(scaleMode);
            }
        }
    }

    public static void setDefaultStyle(VisualConnection connection) {
        connection.setLineWidth(VisualCommonSettings.getConnectionLineWidth());

        connection.setArrow(true);
        connection.setArrowLength(VisualCommonSettings.getConnectionArrowLength());
        connection.setArrowWidth(VisualCommonSettings.getConnectionArrowWidth());

        connection.setBubble(false);
        connection.setBubbleSize(VisualCommonSettings.getConnectionBubbleSize());
    }

    public static void copyShape(VisualConnection srcConnection, VisualConnection dstConnection) {
        copyShape(srcConnection, dstConnection, new Point2D.Double(1.0, 1.0));
    }

    public static void copyShape(VisualConnection srcConnection, VisualConnection dstConnection, Point2D scale) {
        dstConnection.setConnectionType(srcConnection.getConnectionType());
        ConnectionGraphic srcGraphic = srcConnection.getGraphic();
        ConnectionGraphic dstGraphic = dstConnection.getGraphic();
        if (dstGraphic instanceof Polyline polyline) {
            polyline.resetControlPoints();
            for (ControlPoint srcControlPoint: srcGraphic.getControlPoints()) {
                Point2D dstPosition = getScaledPosition(srcControlPoint, scale);
                polyline.addControlPoint(dstPosition);
            }
        } else if (dstGraphic instanceof Bezier dstBezier) {
            Bezier srcBezier = (Bezier) srcGraphic;
            BezierControlPoint[] srcControlPoints = srcBezier.getBezierControlPoints();
            BezierControlPoint[] dstControlPoints = dstBezier.getBezierControlPoints();

            Point2D dst0Position = getScaledPosition(srcControlPoints[0], scale);
            dstControlPoints[0].setPosition(dst0Position);

            Point2D dst1Position = getScaledPosition(srcControlPoints[1], scale);
            dstControlPoints[1].setPosition(dst1Position);
        }
    }

    private static Point2D getScaledPosition(ControlPoint cp, Point2D scale) {
        return new Point2D.Double(cp.getX() * scale.getX(), cp.getY() * scale.getY());
    }

    public static void shapeConnectionAsStep(VisualConnection connection, double dx) {
        if (connection != null) {
            Polyline polyline = (Polyline) connection.getGraphic();
            VisualTransformableNode firstNode = (VisualTransformableNode) connection.getFirst();
            VisualTransformableNode secondNode = (VisualTransformableNode) connection.getSecond();
            Point2D p1 = firstNode.getRootSpacePosition();
            Point2D p2 = secondNode.getRootSpacePosition();
            if (!Geometry.isAligned(p1.getY(), p2.getY())) {
                double x = p1.getX() + dx;
                if (!Geometry.isAligned(p1.getX(), x)) {
                    polyline.addControlPoint(new Point2D.Double(x, p1.getY()));
                }
                if (!Geometry.isAligned(x, p2.getX())) {
                    polyline.addControlPoint(new Point2D.Double(x, p2.getY()));
                }
            }
        }
    }

    public static void shapeConnectionAsBridge(VisualConnection connection, double dx, double dy) {
        shapeConnectionAsBridge(connection, dx, dy, 0.0);
    }

    public static void shapeConnectionAsBridge(VisualConnection connection, double dx1, double dy, double dx2) {
        if (connection != null) {
            Polyline polyline = (Polyline) connection.getGraphic();
            VisualTransformableNode firstNode = (VisualTransformableNode) connection.getFirst();
            VisualTransformableNode secondNode = (VisualTransformableNode) connection.getSecond();
            Point2D p1 = firstNode.getRootSpacePosition();
            Point2D p2 = secondNode.getRootSpacePosition();
            double y = dy + (dy > 0 ? Math.max(p1.getY(), p2.getY()) : Math.min(p1.getY(), p2.getY()));

            double x1 = p1.getX() + dx1;
            if (!Geometry.isNegligible(dx1)) {
                polyline.addControlPoint(new Point2D.Double(x1, p1.getY()));
            }
            polyline.addControlPoint(new Point2D.Double(x1, y));

            double x2 = p2.getX() + dx2;
            polyline.addControlPoint(new Point2D.Double(x2, y));
            if (!Geometry.isNegligible(dx2)) {
                polyline.addControlPoint(new Point2D.Double(x2, p2.getY()));
            }
        }
    }

}
