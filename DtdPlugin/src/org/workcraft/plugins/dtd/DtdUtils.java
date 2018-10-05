package org.workcraft.plugins.dtd;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.shared.CommonVisualSettings;

import java.awt.geom.Point2D;

public class DtdUtils {

    private static final double ARROW_LENGTH = 0.2;
    private static final double CAUSALITY_ARC_OFFSET = 1.0;

    public static TransitionEvent.Direction getNextDirection(Signal.State state) {
        switch (state) {
        case HIGH: return TransitionEvent.Direction.FALL;
        case LOW: return TransitionEvent.Direction.RISE;
        case UNSTABLE: return TransitionEvent.Direction.STABILISE;
        case STABLE: return TransitionEvent.Direction.DESTABILISE;
        default: return null;
        }
    }

    public static TransitionEvent.Direction getPreviousDirection(Signal.State state) {
        switch (state) {
        case HIGH: return TransitionEvent.Direction.RISE;
        case LOW: return TransitionEvent.Direction.FALL;
        case UNSTABLE: return TransitionEvent.Direction.DESTABILISE;
        case STABLE: return TransitionEvent.Direction.STABILISE;
        default: return null;
        }
    }

    public static Signal.State getNextState(TransitionEvent.Direction direction) {
        switch (direction) {
        case RISE: return Signal.State.HIGH;
        case FALL: return Signal.State.LOW;
        case DESTABILISE: return Signal.State.UNSTABLE;
        case STABILISE: return Signal.State.STABLE;
        default: return null;
        }
    }

    public static Signal.State getPreviousState(TransitionEvent.Direction direction) {
        switch (direction) {
        case RISE: return Signal.State.LOW;
        case FALL: return Signal.State.HIGH;
        case DESTABILISE: return Signal.State.STABLE;
        case STABILISE: return Signal.State.UNSTABLE;
        default: return null;
        }
    }

    public static Signal.State getNextState(Event event) {
        if (event instanceof EntryEvent) {
            Signal signal = event.getSignal();
            return signal.getInitialState();
        }
        if (event instanceof TransitionEvent) {
            TransitionEvent transition = (TransitionEvent) event;
            return getNextState(transition.getDirection());
        }
        return null;
    }

    public static boolean isLevelConnection(MathConnection connection) {
        boolean result = false;
        if (connection != null) {
            MathNode c1 = connection.getFirst();
            MathNode c2 = connection.getSecond();
            if ((c1 instanceof Event) && (c2 instanceof Event)) {
                Signal s1 = ((Event) c1).getSignal();
                Signal s2 = ((Event) c2).getSignal();
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

        Signal.State state = null;
        if (v1 instanceof VisualEvent) {
            VisualSignal s1 = ((VisualEvent) v1).getVisualSignal();
            state = s1.getInitialState();
            if (v1 instanceof VisualTransitionEvent) {
                VisualTransitionEvent t1 = (VisualTransitionEvent) v1;
                TransitionEvent.Direction direction = t1.getReferencedTransition().getDirection();
                state = getNextState(direction);
            }
        }
        if ((state == Signal.State.HIGH) || (state == Signal.State.LOW)) {
            double offset = CommonVisualSettings.getNodeSize() * (state == Signal.State.LOW ? 0.25 : -0.25);
            polyline.addControlPoint(new Point2D.Double(v1.getX(), v1.getY() + offset));
            polyline.addControlPoint(new Point2D.Double(v2.getX(), v2.getY() + offset));
        }
    }

    public static boolean isEventConnection(MathConnection connection) {
        boolean result = false;
        if (connection != null) {
            MathNode c1 = connection.getFirst();
            MathNode c2 = connection.getSecond();
            if ((c1 instanceof Event) && (c2 instanceof Event)) {
                Signal s1 = ((Event) c1).getSignal();
                Signal s2 = ((Event) c2).getSignal();
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

    public static VisualLevelConnection getPredVisualLevel(VisualDtd dtd, VisualEvent event) {
        for (Connection eventConnection : dtd.getConnections(event)) {
            if ((eventConnection instanceof VisualLevelConnection) && (eventConnection.getSecond() == event)) {
                return (VisualLevelConnection) eventConnection;
            }
        }
        return null;
    }

    public static VisualLevelConnection getNextVisualLevel(VisualDtd dtd, VisualEvent event) {
        for (Connection eventConnection : dtd.getConnections(event)) {
            if ((eventConnection instanceof VisualLevelConnection) && (eventConnection.getFirst() == event)) {
                return (VisualLevelConnection) eventConnection;
            }
        }
        return null;
    }

    public static VisualEvent getPredVisualEvent(VisualDtd dtd, VisualEvent event) {
        VisualLevelConnection predLevel = getPredVisualLevel(dtd, event);
        if (predLevel != null) {
            Node first = predLevel.getFirst();
            if (first instanceof VisualEvent) {
                return (VisualEvent) first;
            }
        }
        return null;
    }

    public static VisualEvent getNextVisualEvent(VisualDtd dtd, VisualEvent event) {
        VisualLevelConnection nextLevel = getNextVisualLevel(dtd, event);
        if (nextLevel != null) {
            Node second = nextLevel.getSecond();
            if (second instanceof VisualEvent) {
                return (VisualEvent) second;
            }
        }
        return null;
    }

    public static VisualConnection removeTransitionEvent(VisualDtd dtd, VisualTransitionEvent transition) {
        VisualEvent predEvent = DtdUtils.getPredVisualEvent(dtd, transition);
        VisualEvent nextEvent = DtdUtils.getNextVisualEvent(dtd, transition);
        if ((predEvent != null) && (nextEvent != null)) {
            dtd.removeFromSelection(transition);
            dtd.remove(transition);
            try {
                return dtd.connect(predEvent, nextEvent);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static VisualConnection removePrefixTransitionEvents(VisualDtd dtd, VisualEvent endEvent, TransitionEvent.Direction stopDirection) {
        boolean removed = false;
        VisualEvent event = getPredVisualEvent(dtd, endEvent);
        while (event instanceof VisualTransitionEvent) {
            VisualTransitionEvent transition = (VisualTransitionEvent) event;
            if (transition.getDirection() == stopDirection) break;
            VisualEvent predEvent = getPredVisualEvent(dtd, event);
            dtd.removeFromSelection(event);
            dtd.remove(event);
            event = predEvent;
            removed = true;
        }
        if (event instanceof VisualEntryEvent) {
            VisualSignal signal = event.getVisualSignal();
            signal.setInitialState(getNextState(stopDirection));
        }
        if (removed) {
            try {
                return dtd.connect(event, endEvent);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static VisualConnection removeSuffixTransitionEvents(VisualDtd dtd, VisualEvent startEvent) {
        boolean removed = false;
        VisualEvent event = getNextVisualEvent(dtd, startEvent);
        while (event instanceof VisualTransitionEvent) {
            VisualEvent nextEvent = getNextVisualEvent(dtd, event);
            dtd.removeFromSelection(event);
            dtd.remove(event);
            event = nextEvent;
            removed = true;
        }
        if (removed) {
            try {
                return dtd.connect(startEvent, event);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
