package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public final class ConnectionUtils {

    private ConnectionUtils() {
    }

    public static void adjustInsideComponentContactPositions(VisualConnection connection) {
        if ((connection != null) && (connection.getGraphic() instanceof Polyline polyline)) {
            VisualNode first = connection.getFirst();
            VisualNode second = connection.getSecond();
            if (polyline.getControlPointCount() > 0) {
                moveInsideComponentContactsToControlPoints(first, second, polyline);
            } else {
                moveInsideComponentContactsByGradient(first, second);
            }
        }
    }
    private static void moveInsideComponentContactsToControlPoints(
            VisualNode first, VisualNode second, Polyline polyline) {

        ControlPoint firstControlPoint = polyline.getFirstControlPoint();
        Point2D firstPos = firstControlPoint.getRootSpacePosition();
        ControlPoint lastControlPoint = polyline.getLastControlPoint();
        Point2D lastPos = lastControlPoint.getRootSpacePosition();
        if (first instanceof VisualContact firstContact) {
            if (moveContactIfInsideComponent(firstContact, firstPos)) {
                polyline.remove(firstControlPoint);
            }
        }
        if (second instanceof VisualContact secondContact) {
            if (moveContactIfInsideComponent(secondContact, lastPos)) {
                polyline.remove(lastControlPoint);
            }
        }
    }

    private static boolean moveContactIfInsideComponent(VisualContact contact, Point2D pos) {
        Node parent = contact.getParent();
        if (parent instanceof VisualCircuitComponent component) {
            Rectangle2D bb = component.getInternalBoundingBoxInLocalSpace();
            if (bb.contains(contact.getPosition())) {
                contact.setRootSpacePosition(pos);
                double dx = getOffset(contact.getX(), bb.getMinX(), bb.getMaxX());
                double dy = getOffset(contact.getY(), bb.getMinY(), bb.getMaxY());
                if (Math.abs(dy) > Math.abs(dx)) {
                    contact.setDirection(dy > 0 ? VisualContact.Direction.SOUTH : VisualContact.Direction.NORTH);
                } else {
                    contact.setDirection(dx > 0 ? VisualContact.Direction.EAST : VisualContact.Direction.WEST);
                }
                return true;
            }
        }
        return false;
    }

    private static double getOffset(double value, double min, double max) {
        if (value > max) {
            return value - max;
        }
        if (value < min) {
            return value - min;
        }
        return 0;
    }

    private static void moveInsideComponentContactsByGradient(VisualNode first, VisualNode second) {
        Point2D gradient = getGradient(first, second);
        if (gradient != null) {
            if (first instanceof VisualContact firstContact) {
                moveContactOutsideComponent(firstContact, gradient.getX(), gradient.getY());
            }
            if (second instanceof VisualContact secondContact) {
                moveContactOutsideComponent(secondContact, -gradient.getX(), -gradient.getY());
            }
        }
    }

    private static Point2D getGradient(VisualNode first, VisualNode second) {
        if (!(first instanceof VisualComponent) || !(second instanceof VisualComponent)) {
            return null;
        }
        Point2D firstPos = ((VisualComponent) first).getRootSpacePosition();
        Point2D secondPos = ((VisualComponent) second).getRootSpacePosition();
        double dx = secondPos.getX() - firstPos.getX();
        double dy = secondPos.getY() - firstPos.getY();
        return new Point2D.Double(dx, dy);
    }

    private static void moveContactOutsideComponent(VisualContact contact, double dx, double dy) {
        Node parent = contact.getParent();
        if (parent instanceof VisualCircuitComponent component) {
            Rectangle2D bb = component.getInternalBoundingBoxInLocalSpace();
            if (bb.contains(contact.getPosition())) {
                if (Math.abs(dy) > Math.abs(dx)) {
                    VisualContact.Direction direction = (dy > 0) ? VisualContact.Direction.SOUTH
                            : VisualContact.Direction.NORTH;

                    component.setPositionByDirection(contact, direction, dx < 0);
                } else {
                    VisualContact.Direction direction = (dx > 0) ? VisualContact.Direction.EAST
                            : VisualContact.Direction.WEST;

                    component.setPositionByDirection(contact, direction, dy < 0);
                }
            }
        }
    }

}
