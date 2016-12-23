package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.PartialCurveInfo;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;

public class ConnectionHelper {
    public static final double SAME_ANCHOR_POINT_THRESHOLD = 0.1;

    public static boolean areDifferentAnchorPoints(Point2D p1, Point2D p2) {
        return p1.distance(p2) > SAME_ANCHOR_POINT_THRESHOLD;
    }

    public static boolean canBeAnchorPoint(Point2D locationInRootSpace, VisualConnection connection) {
        Point2D first = connection.getFirstCenter();
        Point2D second = connection.getSecondCenter();
        return areDifferentAnchorPoints(locationInRootSpace, first) && areDifferentAnchorPoints(locationInRootSpace, second);
    }

    public static void addControlPoints(VisualConnection connection, List<Point2D> locationsInRootSpace) {
        if ((connection != null) && (connection.getGraphic() instanceof Polyline) && (locationsInRootSpace != null)) {
            Polyline polyline = (Polyline) connection.getGraphic();
            AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
            for (Point2D locationInRootSpace: locationsInRootSpace) {
                if (canBeAnchorPoint(locationInRootSpace, connection)) {
                    Point2D locationInLocalSpace = rootToLocalTransform.transform(locationInRootSpace, null);
                    polyline.addControlPoint(locationInLocalSpace);
                }
            }
        }
    }

    public static void prependControlPoints(VisualConnection connection, List<Point2D> locationsInRootSpace) {
        if ((connection != null) && (connection.getGraphic() instanceof Polyline) && (locationsInRootSpace != null)) {
            Polyline polyline = (Polyline) connection.getGraphic();
            AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
            int segment = 0;
            for (Point2D locationInRootSpace: locationsInRootSpace) {
                if (canBeAnchorPoint(locationInRootSpace, connection)) {
                    Point2D locationInLocalSpace = rootToLocalTransform.transform(locationInRootSpace, null);
                    polyline.insertControlPointInSegment(locationInLocalSpace, segment++);
                }
            }
        }
    }

    public static ControlPoint createControlPoint(VisualConnection connection, Point2D locationInRootSpace) {
        ControlPoint result = null;
        if ((connection != null) && (locationInRootSpace != null)) {
            AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
            Point2D locationInLocalSpace = rootToLocalTransform.transform(locationInRootSpace, null);

            ConnectionGraphic graphic = connection.getGraphic();
            Point2D locationOnConnection = graphic.getNearestPointOnCurve(locationInLocalSpace);
            connection.setConnectionType(ConnectionType.POLYLINE);
            Polyline polyline = (Polyline) connection.getGraphic();
            int segmentIndex = polyline.getNearestSegment(locationInLocalSpace, null);
            result = polyline.insertControlPointInSegment(locationOnConnection, segmentIndex);
        }
        return result;
    }

    public static LinkedList<Point2D> getPrefixControlPoints(VisualConnection connection, Point2D splitPointInLocalSpace) {
        LinkedList<Point2D> locationsInRootSpace = new LinkedList<>();
        if ((connection != null) && (connection.getGraphic() instanceof Polyline) && (splitPointInLocalSpace != null)) {
            Polyline polyline = (Polyline) connection.getGraphic();
            int splitIndex = polyline.getNearestSegment(splitPointInLocalSpace, null);
            AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);
            int index = -1;
            for (ControlPoint cp:  polyline.getControlPoints()) {
                index++;
                if (index < splitIndex) {
                    Point2D locationInLocalSpace = cp.getPosition();
                    if ((index < splitIndex - 1) || areDifferentAnchorPoints(locationInLocalSpace, splitPointInLocalSpace)) {
                        Point2D locationInRootSpace = localToRootTransform.transform(locationInLocalSpace, null);
                        locationsInRootSpace.add(locationInRootSpace);
                    }
                }
            }
        }
        return locationsInRootSpace;
    }

    public static LinkedList<Point2D> getSuffixControlPoints(VisualConnection connection, Point2D splitPointInLocalSpace) {
        LinkedList<Point2D> locationsInRootSpace = new LinkedList<>();
        if ((connection != null) && (connection.getGraphic() instanceof Polyline) && (splitPointInLocalSpace != null)) {
            Polyline polyline = (Polyline) connection.getGraphic();
            int splitIndex = polyline.getNearestSegment(splitPointInLocalSpace, null);
            AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);
            int index = -1;
            for (ControlPoint cp:  polyline.getControlPoints()) {
                index++;
                if (index >= splitIndex) {
                    Point2D locationInLocalSpace = cp.getPosition();
                    if ((index > splitIndex) || areDifferentAnchorPoints(locationInLocalSpace, splitPointInLocalSpace)) {
                        Point2D locationInRootSpace = localToRootTransform.transform(locationInLocalSpace, null);
                        locationsInRootSpace.add(locationInRootSpace);
                    }
                }
            }
        }
        return locationsInRootSpace;
    }

    public static LinkedList<Point2D> getMergedControlPoints(VisualTransformableNode mergeNode, VisualConnection con1, VisualConnection con2) {
        LinkedList<Point2D> locations = new LinkedList<>();
        Point2D lastLocation = null;
        if (con1 != null) {
            if (con1.getGraphic() instanceof Polyline) {
                Polyline polyline = (Polyline) con1.getGraphic();
                AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(con1);
                for (ControlPoint cp:  polyline.getControlPoints()) {
                    Point2D location = localToRootTransform.transform(cp.getPosition(), null);
                    locations.add(location);
                    lastLocation = location;
                }
            }
        }
        Point2D nodeLocation = null;
        if (mergeNode != null) {
            nodeLocation = mergeNode.getRootSpacePosition();
            if ((lastLocation != null) && (nodeLocation.distanceSq(lastLocation) < 0.01)) {
                locations.removeLast();
            }
            locations.add(nodeLocation);
        }
        Point2D firstLocation = null;
        if (con2 != null) {
            if (con2.getGraphic() instanceof Polyline) {
                Polyline polyline = (Polyline) con2.getGraphic();
                AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(con2);
                for (ControlPoint cp:  polyline.getControlPoints()) {
                    Point2D location = localToRootTransform.transform(cp.getPosition(), null);
                    locations.add(location);
                    if ((firstLocation == null) && (nodeLocation != null) && (nodeLocation.distanceSq(location) < 0.01)) {
                        locations.removeLast();
                        firstLocation = location;
                    }
                }
            }
        }
        return locations;
    }

    public static void filterControlPoints(Polyline polyline, double distanceThreshold, double gradientThreshold) {
        PartialCurveInfo curveInfo = polyline.getCurveInfo();
        Point2D startPos = polyline.getPointOnCurve(curveInfo.tStart);
        Point2D endPos = polyline.getPointOnCurve(curveInfo.tEnd);
        // Forward filtering by distance
        filterControlPointsByDistance(polyline, startPos, distanceThreshold, false);
        // Backward filtering by distance
        filterControlPointsByDistance(polyline, endPos, distanceThreshold, true);
        // Filtering by gradient
        int i = 0;
        while (i < polyline.getControlPointCount()) {
            Point2D predPos = startPos;
            if (i > 0) {
                ControlPoint pred = polyline.getControlPoint(i - 1);
                predPos = pred.getPosition();
            }
            Point2D succPos = endPos;
            if (i < polyline.getControlPointCount() - 1) {
                ControlPoint succ = polyline.getControlPoint(i + 1);
                succPos = succ.getPosition();
            }
            ControlPoint cur = polyline.getControlPoint(i);
            Point2D curPos = cur.getPosition();
            if (Math.abs(clacGradient(predPos, curPos, succPos)) < gradientThreshold) {
                polyline.remove(cur);
            } else {
                i++;
            }
        }
    }

    private static void filterControlPointsByDistance(Polyline polyline, Point2D startPos, double threshold, boolean reverse) {
        List<ControlPoint> controlPoints = new LinkedList<>(polyline.getControlPoints());
        if (reverse) {
            Collections.reverse(controlPoints);
        }
        Point2D predPos = startPos;
        for (ControlPoint cp:  controlPoints) {
            Point2D curPos = cp.getPosition();
            if (curPos.distanceSq(predPos) < threshold) {
                polyline.remove(cp);
            } else {
                predPos = curPos;
            }
        }
    }

    public static void moveControlPoints(VisualConnection connection, Point2D offset) {
        if ((connection != null) && (offset != null)) {
            ConnectionGraphic graphic = connection.getGraphic();
            for (ControlPoint cp:  graphic.getControlPoints()) {
                Point2D p1 = cp.getPosition();
                Point2D p2 = new Point2D.Double(p1.getX() + offset.getX(), p1.getY() + offset.getY());
                cp.setPosition(p2);
            }
        }
    }

    public static void removeControlPointsByDistance(Polyline polyline, Point2D pos, double threshold) {
        List<ControlPoint> controlPoints = new LinkedList<>(polyline.getControlPoints());
        for (ControlPoint cp:  controlPoints) {
            Point2D curPos = cp.getPosition();
            if (curPos.distanceSq(pos) < threshold) {
                polyline.remove(cp);
            }
        }
    }

    private static double clacGradient(Point2D p1, Point2D p2, Point2D p3) {
        double p1x = p1.getX();
        double p1y = p1.getY();
        double p2x = p2.getX();
        double p2y = p2.getY();
        double p3x = p3.getX();
        double p3y = p3.getY();
        return p1x * (p2y - p3y) + p2x * (p3y - p1y) + p3x * (p1y - p2y);
    }

    public static VisualConnection getParentConnection(ControlPoint cp) {
        Node graphic = cp.getParent();
        return (graphic == null) ? null : (VisualConnection) graphic.getParent();
    }

}
