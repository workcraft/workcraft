package org.workcraft.plugins.dtd;

import java.awt.geom.Point2D;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.plugins.dtd.Transition.Direction;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class DtdUtils {

    public static boolean isLevelConnection(VisualConnection connection) {
        boolean result = false;
        if (connection != null) {
            VisualComponent v1 = (VisualComponent) connection.getFirst();
            VisualComponent v2 = (VisualComponent) connection.getSecond();
            if (v2 instanceof VisualTransition) {
                Signal s2 = ((VisualTransition) v2).getReferencedTransition().getSignal();
                Signal s1 = null;
                if (v1 instanceof VisualSignal) {
                    s1 = ((VisualSignal) v1).getReferencedSignal();
                } else if (v1 instanceof VisualTransition) {
                    s1 = ((VisualTransition) v1).getReferencedTransition().getSignal();
                }
                result = s1 == s2;
            }
        }
        return result;
    }

    public static void decorateLevelConnection(VisualConnection connection) {
        VisualComponent v1 = (VisualComponent) connection.getFirst();
        VisualComponent v2 = (VisualComponent) connection.getSecond();

        connection.setConnectionType(ConnectionType.POLYLINE);
        Polyline polyline = (Polyline) connection.getGraphic();
        connection.setArrow(false);
        connection.setLineWidth(0.5 * CommonVisualSettings.getStrokeWidth());
        connection.setScaleMode(ScaleMode.LOCK_RELATIVELY);

        double offset = 0.0;
        if (v2 instanceof VisualTransition) {
            Transition t = ((VisualTransition) v2).getReferencedTransition();
            offset = CommonVisualSettings.getBaseSize() * ((t.getDirection() == Direction.MINUS) ? -0.5 : 0.5);
        }
        Point2D cp1 = new Point2D.Double(v1.getRootSpaceX(), v1.getRootSpaceY() + offset);
        Point2D cp2 = new Point2D.Double(v2.getRootSpaceX(), v2.getRootSpaceY() + offset);
        polyline.addControlPoint(cp1);
        polyline.addControlPoint(cp2);
    }

}
