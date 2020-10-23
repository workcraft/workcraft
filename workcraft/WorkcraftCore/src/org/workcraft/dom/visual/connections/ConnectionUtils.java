package org.workcraft.dom.visual.connections;

import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

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
        if (dstGraphic instanceof Polyline) {
            Polyline polyline = (Polyline) dstGraphic;
            polyline.resetControlPoints();
            for (ControlPoint srcControlPoint: srcGraphic.getControlPoints()) {
                Point2D dstPosition = getPosition(srcControlPoint, scale);
                polyline.addControlPoint(dstPosition);
            }
        } else if (dstGraphic instanceof Bezier) {
            Bezier srcBezier = (Bezier) srcGraphic;
            Bezier dstBezier = (Bezier) dstGraphic;
            BezierControlPoint[] srcControlPoints = srcBezier.getBezierControlPoints();
            BezierControlPoint[] dstControlPoints = dstBezier.getBezierControlPoints();

            Point2D dst0Position = getPosition(srcControlPoints[0], scale);
            dstControlPoints[0].setPosition(dst0Position);

            Point2D dst1Position = getPosition(srcControlPoints[1], scale);
            dstControlPoints[1].setPosition(dst1Position);
        }
    }

    private static Point2D getPosition(ControlPoint cp, Point2D scale) {
        return new Point2D.Double(cp.getX() * scale.getX(), cp.getY() * scale.getY());
    }

}
