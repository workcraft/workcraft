package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;

public class ConnectionHelper {

	static public void addControlPoints(VisualConnection connection, List<Point2D> locationsInRootSpace) {
		if ((locationsInRootSpace != null) && (connection.getGraphic() instanceof Polyline)) {
			Polyline polyline = (Polyline)connection.getGraphic();
			AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
			for (Point2D location: locationsInRootSpace) {
				Point2D locationInLocalSpace = rootToLocalTransform.transform(location, null);
				polyline.addControlPoint(locationInLocalSpace);
			}
		}
	}

	static public ControlPoint createControlPoint(VisualConnection connection, Point2D locationInRootSpace) {
		ControlPoint result = null;
		if (locationInRootSpace != null) {
			AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
			Point2D locationInLocalSpace = rootToLocalTransform.transform(locationInRootSpace, null);

			ConnectionGraphic graphic = connection.getGraphic();
			Point2D locationOnConnection = graphic.getNearestPointOnCurve(locationInLocalSpace);
			int segmentIndex = 0;
			Polyline polyline = null;
			if (graphic instanceof Polyline) {
				polyline = (Polyline)graphic;
				segmentIndex = polyline.getNearestSegment(locationInLocalSpace, null);
			} else {
				connection.setConnectionType(ConnectionType.POLYLINE);
				polyline = (Polyline)connection.getGraphic();
			}
			if (polyline != null) {
				result = polyline.insertControlPointInSegment(locationOnConnection, segmentIndex);
			}
		}
		return result;
	}

}
