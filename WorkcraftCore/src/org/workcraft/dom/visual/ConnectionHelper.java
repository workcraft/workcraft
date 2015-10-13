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

	static public LinkedList<Point2D> getPrefixControlPoints(VisualConnection connection, Point2D splitPointInRootSpace) {
		LinkedList<Point2D> locationsInRootSpace = new LinkedList<>();
		if ((connection != null) && (connection.getGraphic() instanceof Polyline) && (splitPointInRootSpace != null)) {
			Polyline polyline = (Polyline)connection.getGraphic();
			AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
			Point2D splitPointInLocalSpace = rootToLocalTransform.transform(splitPointInRootSpace, null);
			int splitIndex = polyline.getNearestSegment(splitPointInLocalSpace, null);
			AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);
			int index = -1;
			for (ControlPoint cp:  polyline.getControlPoints()) {
				Point2D cpLocation = cp.getPosition();
				index++;
				if ((index > splitIndex) || (splitPointInLocalSpace.distanceSq(cpLocation) < 0.001)) {
					break;
				}
				Point2D locationInRootSpace = localToRootTransform.transform(cpLocation, null);
				locationsInRootSpace.add(locationInRootSpace);
			}
		}
		return locationsInRootSpace;
	}

	static public LinkedList<Point2D> getSuffixControlPoints(VisualConnection connection, Point2D splitPointInRootSpace) {
		LinkedList<Point2D> locationsInRootSpace = new LinkedList<>();
		if ((connection != null) && (connection.getGraphic() instanceof Polyline) && (splitPointInRootSpace != null)) {
			Polyline polyline = (Polyline)connection.getGraphic();
			AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
			Point2D splitPointInLocalSpace = rootToLocalTransform.transform(splitPointInRootSpace, null);
			int splitIndex = polyline.getNearestSegment(splitPointInLocalSpace, null);
			AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);
			int index = -1;
			for (ControlPoint cp:  polyline.getControlPoints()) {
				Point2D cpLocation = cp.getPosition();
				index++;
				if ((index < splitIndex) || (splitPointInLocalSpace.distanceSq(cpLocation) < 0.001)) {
					continue;
				}
				Point2D locationInRootSpace = localToRootTransform.transform(cp.getPosition(), null);
				locationsInRootSpace.add(locationInRootSpace);
			}
		}
		return locationsInRootSpace;
	}


	static public LinkedList<Point2D> getMergedControlPoints(VisualTransformableNode mergeNode, VisualConnection con1, VisualConnection con2) {
		LinkedList<Point2D> locations = new LinkedList<>();
		if (con1.getGraphic() instanceof Polyline) {
			Polyline polyline = (Polyline)con1.getGraphic();
			AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(con1);
			for (ControlPoint cp:  polyline.getControlPoints()) {
				Point2D location = localToRootTransform.transform(cp.getPosition(), null);
				locations.add(location);
			}
		}
		locations.add(mergeNode.getPosition());
		if (con2.getGraphic() instanceof Polyline) {
			Polyline polyline = (Polyline)con2.getGraphic();
			AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(con2);
			for (ControlPoint cp:  polyline.getControlPoints()) {
				Point2D location = localToRootTransform.transform(cp.getPosition(), null);
				locations.add(location);
			}
		}
		return locations;
	}

	static public void filterControlPoints(Polyline polyline, double distanceThreshold, double gradientThreshold) {
		PartialCurveInfo curveInfo = polyline.getCurveInfo();
		Point2D startPos = polyline.getPointOnCurve(curveInfo.tStart);
		Point2D endPos = polyline.getPointOnCurve(curveInfo.tEnd);
		{
			// Forward filtering by distance
			List<ControlPoint> controlPoints = new LinkedList<>(polyline.getControlPoints());
			filterControlPointsByDistance(polyline, startPos, controlPoints, distanceThreshold);
		}
		{
			// Backward filtering by distance
			List<ControlPoint> controlPoints = new LinkedList<>(polyline.getControlPoints());
			Collections.reverse(controlPoints);
			filterControlPointsByDistance(polyline, endPos, controlPoints, distanceThreshold);
		}
		{
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
		double result = (p1x * (p2y - p3y) + p2x * (p3y - p1y) + p3x * (p1y - p2y));
		return result;
	}

}
