package org.workcraft.gui.edit.work;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;


/**
 * The <code>Grid</code> class is used to generate and draw the background grid, guidelines,
 * as well as to handle the coordinate "snapping".
 * @author Ivan Poliakov
 *
 */
public class Grid implements ViewportListener {
	protected double majorInterval = 5.0;
	protected double minorIntervalFactor = 0.1;
	protected double intervalScaleFactor = 5;

	protected double magThreshold = 5;
	protected double minThreshold = 2;

	protected Path2D minorLinesPath;
	protected Path2D majorLinesPath;

	protected double[][] majorLinePositions;
	protected double[][] minorLinePositions;

	protected int [][] majorLinePositionsScreen;
	protected int [][] minorLinePositionsScreen;

	protected Stroke stroke;

	protected Color majorLinesColor = new Color (200,200,200);
	protected Color minorLinesColor = new Color (240,240,240);
	protected Color guideLinesColor = Color.RED;

	/**
	 * The list of listeners to be notified in case of viewport parameters change.
	 */
	protected LinkedList<GridListener> listeners;

	public Grid() {
		minorLinesPath = new Path2D.Double();
		majorLinesPath = new Path2D.Double();

		minorLinePositions = new double[2][];
		majorLinePositions = new double[2][];

		minorLinePositionsScreen = new int[2][];
		majorLinePositionsScreen = new int[2][];

		listeners = new LinkedList<GridListener>();

		stroke = new BasicStroke();
	}

	protected void updateGrid(Viewport viewport) {
		Rectangle view = viewport.getShape();

		Point viewLL = new Point (view.x, view.height+view.y);
		Point viewUR = new Point (view.width+view.x, view.y);

		Point2D visibleUL = new Point2D.Double();
		Point2D visibleLR = new Point2D.Double();

		viewport.getInverseTransform().transform(viewLL, visibleUL);
		viewport.getInverseTransform().transform(viewUR, visibleLR);


		double visibleHeight = visibleUL.getY() - visibleLR.getY();

		while (visibleHeight / majorInterval > magThreshold) {
			System.out.println("H: " + visibleHeight +"I: " + majorInterval + "T:" + magThreshold);
			majorInterval *= intervalScaleFactor;
		}

		while (visibleHeight / majorInterval < minThreshold) {
			majorInterval /= intervalScaleFactor;
		}

		int majorBottom  = (int)Math.ceil(visibleLR.getY()/majorInterval);
		int majorTop = (int)Math.floor(visibleUL.getY()/majorInterval);

		int majorLeft = (int)Math.ceil(visibleUL.getX()/majorInterval);
		int majorRight = (int)Math.floor(visibleLR.getX()/majorInterval);

		double minorInterval = majorInterval * minorIntervalFactor;

		int minorLeft = (int)Math.ceil(visibleUL.getX()/minorInterval);
		int minorRight = (int)Math.floor(visibleLR.getX()/minorInterval);

		int minorBottom = (int)Math.ceil(visibleLR.getY()/minorInterval);
		int minorTop = (int)Math.floor(visibleUL.getY()/minorInterval);

		minorLinesPath = new Path2D.Double();
		majorLinesPath = new Path2D.Double();

		minorLinePositions[0] = new double[minorRight-minorLeft+1];
		minorLinePositionsScreen[0] = new int[minorRight-minorLeft+1];

		Point2D p1 = new Point2D.Double(), p2 = new Point();

		for (int x=minorLeft; x<=minorRight; x++) {
			minorLinePositions[0][x-minorLeft] = x*minorInterval;
			p1.setLocation(x*minorInterval, 0);
			viewport.getTransform().transform(p1, p2);
			minorLinePositionsScreen[0][x-minorLeft] = (int)p2.getX();

			minorLinesPath.moveTo(p2.getX(), viewLL.getY());
			minorLinesPath.lineTo(p2.getX(), viewUR.getY());
		}

		minorLinePositions[1] = new double[minorTop-minorBottom+1];
		minorLinePositionsScreen[1] = new int[minorTop-minorBottom+1];


		for (int y=minorBottom; y<=minorTop; y++) {
			minorLinePositions[1][y-minorBottom] = y*minorInterval;
			p1.setLocation(0, y*minorInterval);
			viewport.getTransform().transform(p1, p2);
			minorLinePositionsScreen[1][y-minorBottom] = (int)p2.getY();

			minorLinesPath.moveTo(viewLL.getX(), p2.getY());
			minorLinesPath.lineTo(viewUR.getX(), p2.getY());
		}

		majorLinePositions[0] = new double[majorRight-majorLeft+1];
		majorLinePositionsScreen[0] = new int[majorRight-majorLeft+1];

		for (int x=majorLeft; x<=majorRight; x++) {
			majorLinePositions[0][x-majorLeft] = x*majorInterval;
			p1.setLocation(x*majorInterval, 0);
			viewport.getTransform().transform(p1, p2);


			majorLinePositionsScreen[0][x-majorLeft] = (int)p2.getX();


			majorLinesPath.moveTo((int)p2.getX(), viewLL.getY());
			majorLinesPath.lineTo((int)p2.getX(), viewUR.getY());
		}

		majorLinePositions[1] = new double[majorTop-majorBottom+1];
		majorLinePositionsScreen[1] = new int[majorTop-majorBottom+1];

		for (int y=majorBottom; y<=majorTop; y++) {
			majorLinePositions[1][y-majorBottom] = y*majorInterval;
			p1.setLocation(0, y*majorInterval);
			viewport.getTransform().transform(p1, p2);
			majorLinePositionsScreen[1][y-majorBottom] = (int)p2.getY();

			majorLinesPath.moveTo(viewLL.getX(), p2.getY());
			majorLinesPath.lineTo(viewUR.getX(), p2.getY());
		}

		for (GridListener l : listeners) {
			l.gridChanged(this);
		}
	}

	public void draw (Graphics2D g) {
		g.setStroke(stroke);
		g.setColor(minorLinesColor);
		g.draw(minorLinesPath);
		g.setColor(majorLinesColor);
		g.draw(majorLinesPath);
	}

	public double[][] getMinorLinePositions() {
		return minorLinePositions;
	}

	public double[][] getMajorLinePositions() {
		return majorLinePositions;
	}

	public int[][] getMinorLinePositionsScreen() {
		return minorLinePositionsScreen;
	}

	public int[][] getMajorLinePositionsScreen() {
		return majorLinePositionsScreen;
	}

	@Override
	public void shapeChanged(Viewport sender) {
		updateGrid(sender);
	}

	@Override
	public void viewChanged(Viewport sender) {
		updateGrid(sender);
	}

	/**
	 * Registers a new viewport listener that will be notified if viewport parameters change.
	 * @param listener
	 * The new listener.
	 */
	public void addListener (GridListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a listener.
	 * @param listener
	 * The listener to remove.
	 */
	public void removeListener (GridListener listener) {
		listeners.remove(listener);
	}
}