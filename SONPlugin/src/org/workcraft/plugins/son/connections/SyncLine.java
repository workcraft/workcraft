package org.workcraft.plugins.son.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.connections.PartialCurveInfo;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;
import org.workcraft.gui.Coloriser;
import org.workcraft.util.Geometry;

public class SyncLine extends Polyline {

	private VisualConnectionProperties connectionInfo;
	private PartialCurveInfo curveInfo;
	private Rectangle2D boundingBox = null;

	public SyncLine(VisualConnection parent){
		super(parent);
		connectionInfo = parent;
	}

	@Override
	public void draw(DrawRequest r) {

		Graphics2D g = r.getGraphics();

		curveInfo = Geometry.buildConnectionCurveInfo(connectionInfo, this, 0);

		Path2D connectionPath = new Path2D.Double();

		Point2D startPt = getPointOnCurve(curveInfo.tStart);
		Point2D endPt = getPointOnCurve(curveInfo.tEnd);

		connectionPath.moveTo(startPt.getX(), startPt.getY());

		connectionPath.lineTo(endPt.getX(), endPt.getY());

		Color connColor = Coloriser.colorise(connectionInfo.getDrawColor(), r.getDecoration().getColorisation());
		g.setColor(connColor);

//		g.setStroke(new BasicStroke((float)connectionInfo.getLineWidth()));
		g.setStroke(new BasicStroke( 0.15f , BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
				1.5f, new float[]{ 0.1f , 0.075f,}, 0f));
		g.draw(connectionPath);

		boundingBox = connectionPath.getBounds2D();
		boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
		boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);
		boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
		boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);

	}

	@Override
	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}
}