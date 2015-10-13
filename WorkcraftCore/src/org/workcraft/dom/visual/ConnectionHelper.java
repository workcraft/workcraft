package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
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

	static public void filterControlPointsByDistance(Polyline polyline, double threshold) {
		ArrayList<Point2D> locations = new ArrayList<>();
		PartialCurveInfo curveInfo = polyline.getCurveInfo();
		Point2D startPoint = polyline.getPointOnCurve(curveInfo.tStart);
		Point2D endPoint = polyline.getPointOnCurve(curveInfo.tEnd);

		Point2D predPoint = startPoint;
		for (ControlPoint cp:  polyline.getControlPoints()) {
			Point2D curPoint = cp.getPosition();
			if (curPoint.distanceSq(predPoint) > threshold) {
				locations.add(curPoint);
				predPoint = curPoint;
			}
		}

		if ( !locations.isEmpty() ) {
			if (predPoint.distanceSq(endPoint) < threshold) {
				int lastIdx = locations.size() - 1;
				locations.remove(lastIdx);
			}
		}

		polyline.resetControlPoints();
		for (Point2D location: locations) {
			polyline.addControlPoint(location);
		}
	}

	static public void filterControlPointsByGradient(Polyline polyline, double threshold) {
		ArrayList<Point2D> locations = new ArrayList<>();
		PartialCurveInfo curveInfo = polyline.getCurveInfo();
		Point2D startPoint = polyline.getPointOnCurve(curveInfo.tStart);
		Point2D endPoint = polyline.getPointOnCurve(curveInfo.tEnd);

		Point2D predPoint = startPoint;
		Point2D succPoint = null;
		for (ControlPoint cp:  polyline.getControlPoints()) {
			Point2D curPoint = cp.getPosition();
			if (succPoint != null) {
				locations.add(curPoint);
				predPoint = curPoint;
			}
			predPoint = curPoint;
		}

		if ( !locations.isEmpty() ) {
			if (predPoint.distanceSq(endPoint) < threshold){
				int lastIdx = locations.size() - 1;
				locations.remove(lastIdx);
			}
		}

		polyline.resetControlPoints();
		for (Point2D location: locations) {
			polyline.addControlPoint(location);
		}
	}

}
