package org.workcraft.gui.edit.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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
	protected double minorIntervalFactor = 0.1;
	protected double intervalScaleFactor = 2;

	protected double magThreshold = 5;
	protected double minThreshold = 2.5;

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


	protected double majorInterval = 10.0;
	public double getMajorInterval() {
		return this.majorInterval;
	}

	/**
	 * Set the interval of major grid lines in user-space units.
	 * @param majorInterval
	 * The new interval of major grid lines
	 */
	public void setMajorInterval(double majorInterval) {
		this.majorInterval = majorInterval;
	}

	/**
	 * @return
	 * Dynamic interval scaling factor
	 */
	public double getIntervalScaleFactor() {
		return this.intervalScaleFactor;
	}

	/**
	 * Set the dynamic interval scale factor. The major grid line interval will be multiplied or divided by this amount
	 * when applying dynamic grid scaling.
	 * @param intervalScaleFactor
	 */
	public void setIntervalScaleFactor(double intervalScaleFactor) {
		this.intervalScaleFactor = intervalScaleFactor;
	}

	/**
	 * @return
	 * The interval magnification threshold
	 */
	public double getMagThreshold() {
		return this.magThreshold;
	}

	/**
	 * Set the interval magnification threshold. The grid interval will be increased by <code>intervalScaleFactor</code>  if more than <code>magThreshold</code>
	 * major grid intervals become visible across the vertical dimension of the viewport.
	 * @param magThreshold
	 * The new interval magnification threshold.
	 */
	public void setMagThreshold(double magThreshold) {
		this.magThreshold = magThreshold;
	}

	/**
	 * @return
	 * The interval minimisation threshold.
	 */
	public double getMinThreshold() {
		return this.minThreshold;
	}

	/**
	 * Set the interval minimisation threshold. The grid interval will be decreased by <code>intervalScaleFactor</code> if less than <i>minThreshold</i>
	 * major grid intervals become visible across the vertical dimension of the viewport.
	 * major grid intervals become visible.
	 * @param minThreshold
	 */
	public void setMinThreshold(double minThreshold) {
		this.minThreshold = minThreshold;
	}

	/**
	 * @return
	 * The major grid lines drawing color.
	 */
	public Color getMajorLinesColor() {
		return this.majorLinesColor;
	}

	/**
	 * Set the major grid lines drawing color.
	 * @param majorLinesColor
	 * The new color
	 */
	public void setMajorLinesColor(Color majorLinesColor) {
		this.majorLinesColor = majorLinesColor;
	}

	/**
	 * @return
	 * The minor grid lines drawing color.
	 */
	public Color getMinorLinesColor() {
		return this.minorLinesColor;
	}

	/**
	 *  Set the minor grid lines drawing color.
	 * @param minorLinesColor
	 *  The new color
	 */
	public void setMinorLinesColor(Color minorLinesColor) {
		this.minorLinesColor = minorLinesColor;
	}

	/**
	 * The list of listeners to be notified in case of grid parameters change.
	 */
	protected LinkedList<GridListener> listeners;

	/**
	 * Constructs a grid with default parameters:
	 * <ul>
	 * <li> Major grid lines interval = 10 units
	 * <li> Minor grid lines frequency = 10 per major line interval
	 * <li> Dynamic scaling factor = 2
	 * <li> Dynamic magnification threshold = 5 (see <code>setMagThreshold</code>)
	 * <li> Dynamic minimisation threshold = 2.5 ( see <code>setMinThreshold</code>)
	 * </ul>
	 **/
	public Grid() {
		this.minorLinesPath = new Path2D.Double();
		this.majorLinesPath = new Path2D.Double();

		this.minorLinePositions = new double[2][];
		this.majorLinePositions = new double[2][];

		this.minorLinePositionsScreen = new int[2][];
		this.majorLinePositionsScreen = new int[2][];

		this.listeners = new LinkedList<GridListener>();

		this.stroke = new BasicStroke();
	}

	/**
	 * Recalculates visible grid lines based on the viewport parameters.
	 * @param viewport
	 * The viewport to calculate gridlines for.
	 */
	protected void updateGrid(Viewport viewport) {
		Rectangle view = viewport.getShape();


		// Compute the visible user space area from the viewport

		Point2D visibleUL = new Point2D.Double();
		Point2D visibleLR = new Point2D.Double();
		Point viewLL = new Point (view.x, view.height+view.y);
		Point viewUR = new Point (view.width+view.x, view.y);
		viewport.getInverseTransform().transform(viewLL, visibleUL);
		viewport.getInverseTransform().transform(viewUR, visibleLR);


		// Dynamic line interval scaling

		double visibleHeight = visibleUL.getY() - visibleLR.getY();
		while (visibleHeight / this.majorInterval > this.magThreshold)
			this.majorInterval *= this.intervalScaleFactor;
		while (visibleHeight / this.majorInterval < this.minThreshold)
			this.majorInterval /= this.intervalScaleFactor;


		// Compute the leftmost, rightmost, topmost and bottom visible grid lines

		int majorBottom  = (int)Math.ceil(visibleLR.getY()/this.majorInterval);
		int majorTop = (int)Math.floor(visibleUL.getY()/this.majorInterval);

		int majorLeft = (int)Math.ceil(visibleUL.getX()/this.majorInterval);
		int majorRight = (int)Math.floor(visibleLR.getX()/this.majorInterval);

		double minorInterval = this.majorInterval * this.minorIntervalFactor;

		int minorLeft = (int)Math.ceil(visibleUL.getX()/minorInterval);
		int minorRight = (int)Math.floor(visibleLR.getX()/minorInterval);

		int minorBottom = (int)Math.ceil(visibleLR.getY()/minorInterval);
		int minorTop = (int)Math.floor(visibleUL.getY()/minorInterval);



		// Build the gridlines positions, store them as user-space coordinates, screen-space coordinates,
		// and as a drawable path (in screen-space)

		this.minorLinesPath = new Path2D.Double();
		this.majorLinesPath = new Path2D.Double();

		this.minorLinePositions[0] = new double[minorRight-minorLeft+1];
		this.minorLinePositionsScreen[0] = new int[minorRight-minorLeft+1];

		Point2D p1 = new Point2D.Double(), p2 = new Point();

		for (int x=minorLeft; x<=minorRight; x++) {
			this.minorLinePositions[0][x-minorLeft] = x*minorInterval;
			p1.setLocation(x*minorInterval, 0);
			viewport.getTransform().transform(p1, p2);
			this.minorLinePositionsScreen[0][x-minorLeft] = (int)p2.getX();

			this.minorLinesPath.moveTo(p2.getX(), viewLL.getY());
			this.minorLinesPath.lineTo(p2.getX(), viewUR.getY());
		}

		this.minorLinePositions[1] = new double[minorTop-minorBottom+1];
		this.minorLinePositionsScreen[1] = new int[minorTop-minorBottom+1];


		for (int y=minorBottom; y<=minorTop; y++) {
			this.minorLinePositions[1][y-minorBottom] = y*minorInterval;
			p1.setLocation(0, y*minorInterval);
			viewport.getTransform().transform(p1, p2);
			this.minorLinePositionsScreen[1][y-minorBottom] = (int)p2.getY();

			this.minorLinesPath.moveTo(viewLL.getX(), p2.getY());
			this.minorLinesPath.lineTo(viewUR.getX(), p2.getY());
		}

		this.majorLinePositions[0] = new double[majorRight-majorLeft+1];
		this.majorLinePositionsScreen[0] = new int[majorRight-majorLeft+1];

		for (int x=majorLeft; x<=majorRight; x++) {
			this.majorLinePositions[0][x-majorLeft] = x*this.majorInterval;
			p1.setLocation(x*this.majorInterval, 0);
			viewport.getTransform().transform(p1, p2);


			this.majorLinePositionsScreen[0][x-majorLeft] = (int)p2.getX();


			this.majorLinesPath.moveTo((int)p2.getX(), viewLL.getY());
			this.majorLinesPath.lineTo((int)p2.getX(), viewUR.getY());
		}

		this.majorLinePositions[1] = new double[majorTop-majorBottom+1];
		this.majorLinePositionsScreen[1] = new int[majorTop-majorBottom+1];

		for (int y=majorBottom; y<=majorTop; y++) {
			this.majorLinePositions[1][y-majorBottom] = y*this.majorInterval;
			p1.setLocation(0, y*this.majorInterval);
			viewport.getTransform().transform(p1, p2);
			this.majorLinePositionsScreen[1][y-majorBottom] = (int)p2.getY();

			this.majorLinesPath.moveTo(viewLL.getX(), p2.getY());
			this.majorLinesPath.lineTo(viewUR.getX(), p2.getY());
		}


		for (GridListener l : this.listeners)
			l.gridChanged(this);
	}

	/**
	 * Draws the grid. <i>Note that this drawing procedure assumes that the Graphics2D object will draw in screen coordinates.</i>
	 * Please restore the graphics transform object to the original state before calling this method.
	 * @param g
	 * Graphics2D object for the component the viewport is drawn onto.
	 */
	public void draw (Graphics2D g) {
		g.setStroke(this.stroke);
		g.setColor(this.minorLinesColor);
		g.draw(this.minorLinesPath);
		g.setColor(this.majorLinesColor);
		g.draw(this.majorLinesPath);
	}

	/**
	 * Returns minor grid lines positions <i>in user space, double precision</i> as a 2-dimensional array. First row of the array contains x-coordinates of the vertical grid lines,
	 * second row contains y-coordinates of the horizontal grid lines.
	 *
	 * @return
	 * getMinorLinePositions()[0] - the array containing vertical grid lines positions
	 * getMinorLinePositions()[1] - the array containing horizontal grid lines positions
	 *
	 */
	public double[][] getMinorLinePositions() {
		return this.minorLinePositions;
	}

	/**
	 * Returns major grid lines positions <i>in user space, double precision</i> as a 2-dimensional array. First row of the array contains x-coordinates of the vertical grid lines,
	 * second row contains y-coordinates of the horizontal grid lines.
	 *
	 * @return
	 * getMajorLinePositions()[0] - the array containing vertical grid lines positions
	 * getMajorLinePositions()[1] - the array containing horizontal grid lines positions
	 *
	 */
	public double[][] getMajorLinePositions() {
		return this.majorLinePositions;
	}

	/**
	 * Returns minor grid lines positions <i>in screen space space, integer precision</i> as a 2-dimensional array.
	 * First row of the array contains x-coordinates of the vertical grid lines, second row contains y-coordinates of the horizontal grid lines,
	 * @return
	 * getMinorLinePositionsScreen()[0] - the array containing vertical grid lines positions
	 * getMinorLinePositionsScreen()[1] - the array containing horizontal grid lines positions
	 *
	 */
	public int[][] getMinorLinePositionsScreen() {
		return this.minorLinePositionsScreen;
	}


	/**
	 * Returns major grid lines positions <i>in screen space space, integer precision</i> as a 2-dimensional array. First row of the array contains Y-coordinates of the horizontal grid lines,
	 * second row contains X-coordinates of the vertical grid lines.
	 *
	 * @return
	 * getMajorLinePositionsScreen()[0] - the array containing vertical grid lines positions
	 * getMajorLinePositionsScreen()[1] - the array containing horizontal grid lines positions
	 *
	 */
	public int[][] getMajorLinePositionsScreen() {
		return this.majorLinePositionsScreen;
	}


	public void shapeChanged(Viewport sender) {
		updateGrid(sender);
	}


	public void viewChanged(Viewport sender) {
		updateGrid(sender);
	}

	/**
	 * Registers a new grid listener that will be notified if grid parameters change.
	 * @param listener
	 * The new listener.
	 */
	public void addListener (GridListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Removes a listener.
	 * @param listener
	 * The listener to remove.
	 */
	public void removeListener (GridListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Snap coordinate to the closest minor grid line.
	 * @param x coordinate value
	 * @return snapped coordinate value
	 */
	public double snapCoordinate(double x) {
		double m = this.majorInterval*this.minorIntervalFactor;
		return Math.floor(x/m+0.5)*m;
	}
}