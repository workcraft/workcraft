package org.workcraft.plugins.son.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;

public class SyncLine extends Polyline {


	public SyncLine(VisualConnection parent){
		super(parent);
	}

	@Override
	public void draw(DrawRequest r) {

		Graphics2D g = r.getGraphics();

		if (!valid)
			update();

		Path2D connectionPath = new Path2D.Double();

		int start = getSegmentIndex(curveInfo.tStart);
		int end = getSegmentIndex(curveInfo.tEnd);

		Point2D startPt = getPointOnCurve(curveInfo.tStart);
		Point2D endPt = getPointOnCurve(curveInfo.tEnd);

		connectionPath.moveTo(startPt.getX(), startPt.getY());

		for (int i=start; i<end; i++) {
			Line2D segment = getSegment(i);
			connectionPath.lineTo(segment.getX2(), segment.getY2());
		}

		connectionPath.lineTo(endPt.getX(), endPt.getY());

		Color color = Coloriser.colorise(connectionInfo.getDrawColor(), r.getDecoration().getColorisation());
		g.setColor(color);

//		g.setStroke(new BasicStroke((float)connectionInfo.getLineWidth()));
		g.setStroke(new BasicStroke( 0.15f , BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
				1.5f, new float[]{ 0.1f , 0.075f,}, 0f));
		g.draw(connectionPath);

		if (connectionInfo.hasBubble()) {
			DrawHelper.drawBubbleHead(g, curveInfo.headPosition, curveInfo.headOrientation,
					connectionInfo.getBubbleSize(),	color, connectionInfo.getStroke());
		}

	}

}