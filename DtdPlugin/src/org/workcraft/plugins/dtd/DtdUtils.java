package org.workcraft.plugins.dtd;

import java.awt.geom.Point2D;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.plugins.dtd.Signal.State;
import org.workcraft.plugins.dtd.Transition.Direction;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class DtdUtils {

    private static final double ARROW_LENGTH = 0.2;
    private static final double CAUSALITY_ARC_OFFSET = 1.0;

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

        State state = null;
        if (v1 instanceof VisualSignal) {
            VisualSignal s1 = (VisualSignal) v1;
            state = s1.getInitialState();
        } else if (v1 instanceof VisualTransition) {
            VisualTransition t1 = (VisualTransition) v1;
            state = t1.getNextState();
        }
        if ((state == State.HIGH) || (state == State.LOW)) {
            double offset = CommonVisualSettings.getNodeSize() * (state == State.LOW ? 0.5 : -0.5);
            polyline.addControlPoint(new Point2D.Double(v1.getX(), v1.getY() + offset));
            polyline.addControlPoint(new Point2D.Double(v2.getX(), v2.getY() + offset));
        }
    }

    public static Direction getNextDirection(State state) {
        switch (state) {
        case HIGH: return Direction.FALL;
        case LOW: return Direction.RISE;
        case UNSTABLE: return Direction.STABILISE;
        case STABLE: return Direction.DESTABILISE;
        }
        return null;
    }

    public static boolean isEventConnection(MathConnection connection) {
        boolean result = false;
        if (connection != null) {
            MathNode c1 = (MathNode) connection.getFirst();
            MathNode c2 = (MathNode) connection.getSecond();
            if ((c1 instanceof Transition) && (c2 instanceof Transition)) {
                Signal s1 = ((Transition) c1).getSignal();
                Signal s2 = ((Transition) c2).getSignal();
                result = s1 != s2;
            }
        }
        return result;
    }

    public static boolean isVisualEventConnection(VisualConnection connection) {
        return isEventConnection(connection.getReferencedConnection());
    }

    public static void decorateVisualEventConnection(VisualConnection connection) {
        VisualComponent v1 = (VisualComponent) connection.getFirst();
        VisualComponent v2 = (VisualComponent) connection.getSecond();
        connection.setConnectionType(ConnectionType.BEZIER);
        connection.setArrowLength(ARROW_LENGTH);
        Bezier bezier = (Bezier) connection.getGraphic();
        BezierControlPoint[] cp = bezier.getBezierControlPoints();
        Point2D p1 = new Point2D.Double(v1.getRootSpaceX() + CAUSALITY_ARC_OFFSET, v1.getRootSpaceY());
        cp[0].setRootSpacePosition(p1);
        Point2D p2 = new Point2D.Double(v2.getRootSpaceX() - CAUSALITY_ARC_OFFSET, v2.getRootSpaceY());
        cp[1].setRootSpacePosition(p2);
    }

}
