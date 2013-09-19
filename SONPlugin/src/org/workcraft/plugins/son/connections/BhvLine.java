package org.workcraft.plugins.son.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.connections.PartialCurveInfo;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;
import org.workcraft.gui.Coloriser;
import org.workcraft.util.Geometry;

public class BhvLine extends Polyline{

	private Rectangle2D boundingBox = null;
	private GlyphVector glyphVector;
	private static Font labelFont;

	private VisualConnectionProperties connectionInfo;
	private PartialCurveInfo curveInfo;

	static {
		try {
			labelFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.8f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BhvLine(VisualConnection parent){
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

		Color color = Coloriser.colorise(connectionInfo.getDrawColor(), r.getDecoration().getColorisation());

		g.setColor(color);

		g.setStroke(new BasicStroke( 0.02f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
				2.0f, new float[]{ 0.24f , 0.15f,}, 0f));
		g.draw(connectionPath);

		glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "b");

		g.drawGlyphVector(glyphVector, (float)this.getCenter().getX(), (float)this.getCenter().getY());


		boundingBox = connectionPath.getBounds2D();
		boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
		boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);
		boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
		boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);

		if (connectionInfo.hasArrow())
			DrawHelper.drawArrowHead(g, curveInfo.headPosition,	curveInfo.headOrientation,
					connectionInfo.getArrowLength(), connectionInfo.getArrowWidth(), color);

	}

	@Override
	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}

	@Override
	public void createControlPoint(Point2D userLocation){

	}

}
