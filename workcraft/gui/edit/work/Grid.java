package org.workcraft.gui.edit.work;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;


/**
 * The <code>Grid</code> class is used to generate and draw the background grid, as well as providing
 * support for "snap-to-grid" functionality.
 * @author Ivan Poliakov
 *
 */
public class Grid {
	protected boolean showGrid = true;
	protected boolean showGuides = true;

	protected double majorInterval = 5.0;
	protected double minorInterval = 1.0;

	//protected int minorLeft, minorRight, minorTop, minorBottom, majorLeft, majorRight, majorTop, majorBottom;

	public void draw (Graphics2D g, Viewport viewport) {
		Rectangle view = viewport.getShape();

		Point viewLL = new Point (0, view.height);
		Point viewUR = new Point (view.width, 0);

		Point2D visibleLL = new Point2D.Double();
		Point2D visibleUR = new Point2D.Double();

		viewport.getInverseTransform().transform(viewLL, visibleLL);
		viewport.getInverseTransform().transform(viewUR, visibleUR);

		int minorLeft = (int)Math.ceil(visibleLL.getX()/minorInterval);
		int minorRight = (int)Math.floor(visibleUR.getX()/minorInterval);
		int minorBottom = (int)Math.floor(visibleUR.getY()/minorInterval);
		int minorTop = (int)Math.ceil(visibleLL.getY()/minorInterval);

		int majorLeft = (int)Math.ceil(visibleLL.getX()/majorInterval);
		int majorRight = (int)Math.floor(visibleUR.getX()/majorInterval);
		int majorBottom  = (int)Math.floor(visibleUR.getY()/majorInterval);
		int majorTop = (int)Math.ceil(visibleLL.getY()/majorInterval);

		Path2D minorLines = new Path2D.Double(), majorLines = new Path2D.Double();

		for (int x=minorLeft; x<=minorRight; x++) {
			minorLines.moveTo(x*minorInterval, visibleUR.getY());
			minorLines.lineTo(x*minorInterval, visibleLL.getY());
		}

		for (int y=minorBottom; y<=minorTop; y++) {
			minorLines.moveTo(visibleLL.getX(), y*minorInterval);
			minorLines.lineTo(visibleUR.getX(), y*minorInterval);
		}


		for (int x=majorLeft; x<=majorRight; x++) {
			majorLines.moveTo(x*majorInterval, visibleUR.getY());
			majorLines.lineTo(x*majorInterval, visibleLL.getY());
		}

		for (int y=majorBottom; y<=majorTop; y++) {
			majorLines.moveTo(visibleLL.getX(), y*majorInterval);
			majorLines.lineTo(visibleUR.getX(), y*majorInterval);
		}


		Point2D width = viewport.distInUserSpace(new Point (1,2));

		g.setStroke(new BasicStroke((float)width.getX()));
		g.draw(minorLines);
		g.setStroke(new BasicStroke((float)width.getY()));
		g.draw(majorLines);
	}
}