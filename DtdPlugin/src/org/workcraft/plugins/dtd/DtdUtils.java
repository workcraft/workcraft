package org.workcraft.plugins.dtd;

import java.awt.geom.Point2D;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.plugins.dtd.Transition.Direction;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class DtdUtils {

    public static boolean isLevelConnection(MathConnection connection) {
        boolean result = false;
        if (connection != null) {
            MathNode c1 = (MathNode) connection.getFirst();
            MathNode c2 = (MathNode) connection.getSecond();
            if (c2 instanceof Transition) {
                Signal s2 = ((Transition) c2).getSignal();
                Signal s1 = null;
                if (c1 instanceof Signal) {
                    s1 = (Signal) c1;
                } else if (c1 instanceof Transition) {
                    s1 = ((Transition) c1).getSignal();
                }
                result = s1 == s2;
            }
        }
        return result;
    }

    public static boolean isVisualLevelConnection(VisualConnection connection) {
        return isLevelConnection(connection.getReferencedConnection());
    }

    public static void decorateVisualLevelConnection(VisualConnection connection) {
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
