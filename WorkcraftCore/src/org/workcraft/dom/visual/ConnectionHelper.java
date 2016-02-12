package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.PartialCurveInfo;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;

public class ConnectionHelper {
    public static final double SAME_ANCHOR_POINT_THRESHOLD = 0.1;

    static public void addControlPoints(VisualConnection connection, List<Point2D> locationsInRootSpace) {
        if ((connection != null) && (connection.getGraphic() instanceof Polyline) && (locationsInRootSpace != null)) {
            Polyline polyline = (Polyline)connection.getGraphic();
            AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
            for (Point2D location: locationsInRootSpace) {
                if ((location.distance(connection.getFirstCenter()) > SAME_ANCHOR_POINT_THRESHOLD)
                        && (location.distance(connection.getSecondCenter()) > SAME_ANCHOR_POINT_THRESHOLD)) {
                    Point2D locationInLocalSpace = rootToLocalTransform.transform(location, null);
                    polyline.addControlPoint(locationInLocalSpace);
                }
            }
        }
    }

    static public ControlPoint createControlPoint(VisualConnection connection, Point2D locationInRootSpace) {
        ControlPoint result = null;
        if ((connection != null) && (locationInRootSpace != null)) {
            AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
            Point2D locationInLocalSpace = rootToLocalTransform.transform(locationInRootSpace, null);

            ConnectionGraphic graphic = connection.getGraphic();
            Point2D locationOnConnection = graphic.getNearestPointOnCurve(locationInLocalSpace);
            connection.setConnectionType(ConnectionType.POLYLINE);
            Polyline polyline = (Polyline)connection.getGraphic();
            int segmentIndex = polyline.getNearestSegment(locationInLocalSpace, null);
            result = polyline.insertControlPointInSegment(locationOnConnection, segmentIndex);
        }
        return result;
    }

    static public LinkedList<Point2D> getPrefixControlPoints(VisualConnection connection, Point2D splitPointInLocalSpace) {
        LinkedList<Point2D> locationsInRootSpace = new LinkedList<>();
        if ((connection != null) && (connection.getGraphic() instanceof Polyline) && (splitPointInLocalSpace != null)) {
            Polyline polyline = (Polyline)connection.getGraphic();
            int splitIndex = polyline.getNearestSegment(splitPointInLocalSpace, null);
            AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);
            int index = -1;
            for (ControlPoint cp:  polyline.getControlPoints()) {
                index++;
                Point2D locationInLocalSpace = cp.getPosition();
                if ((index <= splitIndex) && (splitPointInLocalSpace.distanceSq(locationInLocalSpace) > 0.01)) {
                    Point2D locationInRootSpace = localToRootTransform.transform(locationInLocalSpace, null);
                    locationsInRootSpace.add(locationInRootSpace);
                }
            }
        }
        return locationsInRootSpace;
    }

    static public LinkedList<Point2D> getSuffixControlPoints(VisualConnection connection, Point2D splitPointInLocalSpace) {
        LinkedList<Point2D> locationsInRootSpace = new LinkedList<>();
        if ((connection != null) && (connection.getGraphic() instanceof Polyline) && (splitPointInLocalSpace != null)) {
            Polyline polyline = (Polyline)connection.getGraphic();
            int splitIndex = polyline.getNearestSegment(splitPointInLocalSpace, null);
            AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);
            int index = -1;
            for (ControlPoint cp:  polyline.getControlPoints()) {
                index++;
                Point2D locationInLocalSpace = cp.getPosition();
                if ((index >= splitIndex) && (splitPointInLocalSpace.distanceSq(locationInLocalSpace) > 0.01)) {
                    Point2D locationInRootSpace = localToRootTransform.transform(locationInLocalSpace, null);
                    locationsInRootSpace.add(locationInRootSpace);
                }
            }
        }
        return locationsInRootSpace;
    }

    static public LinkedList<Point2D> getMergedControlPoints(VisualTransformableNode mergeNode, VisualConnection con1, VisualConnection con2) {
        LinkedList<Point2D> locations = new LinkedList<>();
        Point2D lastLocation = null;
        if (con1.getGraphic() instanceof Polyline) {
            Polyline polyline = (Polyline)con1.getGraphic();
            AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(con1);
            for (ControlPoint cp:  polyline.getControlPoints()) {
                Point2D location = localToRootTransform.transform(cp.getPosition(), null);
                locations.add(location);
                lastLocation = location;
            }
        }
        Point2D nodeLocation = mergeNode.getRootSpacePosition();
        if ((lastLocation != null) && (nodeLocation.distanceSq(lastLocation) < 0.01)) {
            locations.removeLast();
        }
        locations.add(nodeLocation);
        Point2D firstLocation = null;
        if (con2.getGraphic() instanceof Polyline) {
            Polyline polyline = (Polyline)con2.getGraphic();
            AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(con2);
            for (ControlPoint cp:  polyline.getControlPoints()) {
                Point2D location = localToRootTransform.transform(cp.getPosition(), null);
                locations.add(location);
                if ((firstLocation == null) && (nodeLocation.distanceSq(location) < 0.01)) {
                    locations.removeLast();
                    firstLocation = location;
                }
            }
        }
        return locations;
    }

    static public void filterControlPoints(Polyline polyline, double distanceThreshold, double gradientThreshold) {
        PartialCurveInfo curveInfo = polyline.getCurveInfo();
        Point2D startPos = polyline.getPointOnCurve(curveInfo.tStart);
        Point2D endPos = polyline.getPointOnCurve(curveInfo.tEnd);
        // Forward filtering by distance
        List<ControlPoint> controlPoints = new LinkedList<>(polyline.getControlPoints());
        filterControlPointsByDistance(polyline, startPos, controlPoints, distanceThreshold);
        // Backward filtering by distance
        controlPoints = new LinkedList<>(polyline.getControlPoints());
        Collections.reverse(controlPoints);
        filterControlPointsByDistance(polyline, endPos, controlPoints, distanceThreshold);
        // Filtering by gradient
        int i = 0;
        while (i < polyline.getControlPointCount()) {
            Point2D predPos = startPos;
            if (i > 0) {
                ControlPoint pred = polyline.getControlPoint(i-1);
                predPos = pred.getPosition();
            }
            Point2D succPos = endPos;
            if (i < polyline.getControlPointCount() - 1) {
                ControlPoint succ = polyline.getControlPoint(i+1);
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

    private static void filterControlPointsByDistance(Polyline polyline, Point2D startPos,
            List<ControlPoint> controlPoints, double threshold) {

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

    static private double clacGradient(Point2D p1, Point2D p2, Point2D p3) {
        double p1x = p1.getX();
        double p1y = p1.getY();
        double p2x = p2.getX();
        double p2y = p2.getY();
        double p3x = p3.getX();
        double p3y = p3.getY();
        double result = p1x * (p2y - p3y) + p2x * (p3y - p1y) + p3x * (p1y - p2y);
        return result;
    }

}
