package org.workcraft.gui.editor;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 * The <code>Grid</code> class is used to generate and draw the background grid, guidelines,
 * as well as to handle the coordinate "snapping".
 */
public class Grid implements ViewportListener {
    protected double minorIntervalFactor = 0.1;
    protected double intervalScaleFactor = 2;

    protected double maxThreshold = 5;
    protected double minThreshold = 2.5;

    protected Path2D minorLinesPath;
    protected Path2D majorLinesPath;

    protected double[][] majorLinePositions;
    protected double[][] minorLinePositions;

    protected int[][] majorLinePositionsScreen;
    protected int[][] minorLinePositionsScreen;

    protected Stroke stroke;

    protected double majorInterval = 10.0;

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
        return intervalScaleFactor;
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
        return maxThreshold;
    }

    /**
     * Set the interval magnification threshold. The grid interval will be increased by <code>intervalScaleFactor</code>  if more than <code>magThreshold</code>
     * major grid intervals become visible across the vertical dimension of the viewport.
     * @param magThreshold
     * The new interval magnification threshold.
     */
    public void setMagThreshold(double magThreshold) {
        this.maxThreshold = magThreshold;
    }

    /**
     * @return
     * The interval minimisation threshold.
     */
    public double getMinThreshold() {
        return minThreshold;
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
     * <li> Dynamic minimisation threshold = 2.5 (see <code>setMinThreshold</code>)
     * </ul>
     **/
    public Grid() {
        minorLinesPath = new Path2D.Double();
        majorLinesPath = new Path2D.Double();

        minorLinePositions = new double[2][];
        majorLinePositions = new double[2][];

        minorLinePositionsScreen = new int[2][];
        majorLinePositionsScreen = new int[2][];

        listeners = new LinkedList<>();

        stroke = new BasicStroke();
    }

    /**
     * Recalculates visible grid lines based on the viewport parameters.
     * @param viewport
     * The viewport to calculate gridlines for.
     */
    public void updateGrid(Viewport viewport) {
        Rectangle view = viewport.getShape();
        if (view.isEmpty()) return;

        // Compute the visible user space area from the viewport
        Point2D visibleUL = new Point2D.Double();
        Point2D visibleLR = new Point2D.Double();
        Point viewLL = new Point(view.x, view.height + view.y);
        Point viewUR = new Point(view.width + view.x, view.y);
        viewport.getInverseTransform().transform(viewLL, visibleUL);
        viewport.getInverseTransform().transform(viewUR, visibleLR);

        // Dynamic line interval scaling
        double visibleHeight = visibleUL.getY() - visibleLR.getY();
        if (majorInterval > 0) {
            while (visibleHeight / majorInterval > maxThreshold) {
                majorInterval *= intervalScaleFactor;
            }
            while (visibleHeight / majorInterval < minThreshold) {
                majorInterval /= intervalScaleFactor;
            }
        }

        if (EditorCommonSettings.getLightGrid()) {
            updateGridMinorCrosses(viewport, majorInterval * minorIntervalFactor);
            updateGridMajorCrosses(viewport, majorInterval);
        } else {
            updateGridMinorLines(viewport, majorInterval * minorIntervalFactor);
            updateGridMajorLines(viewport, majorInterval);
        }

        for (GridListener l : listeners) {
            l.gridChanged(this);
        }
    }

    private void updateGridMinorCrosses(Viewport viewport, double interval) {
        minorLinesPath = new Path2D.Double();
        double radius = Math.max(1.0, EditorCommonSettings.getLightGridSize() * SizeHelper.getScreenDpmm() / 3.0);
        updateGridCrosses(viewport, interval, radius, minorLinePositions, minorLinePositionsScreen, minorLinesPath);
    }

    private void updateGridMajorCrosses(Viewport viewport, double interval) {
        majorLinesPath = new Path2D.Double();
        double radius = Math.max(1.0, EditorCommonSettings.getLightGridSize() * SizeHelper.getScreenDpmm() / 2.0);
        updateGridCrosses(viewport, interval, radius, majorLinePositions, majorLinePositionsScreen, majorLinesPath);
    }

    private void updateGridCrosses(Viewport viewport, double interval, double crossRadius,
            double[][] linePositions, int[][] linePositionsScreen, Path2D linesPath) {

        Rectangle view = viewport.getShape();

        // Compute the visible user space area from the viewport
        Point2D visibleUL = new Point2D.Double();
        Point2D visibleLR = new Point2D.Double();
        Point viewLL = new Point(view.x, view.height + view.y);
        Point viewUR = new Point(view.width + view.x, view.y);
        viewport.getInverseTransform().transform(viewLL, visibleUL);
        viewport.getInverseTransform().transform(viewUR, visibleLR);

        // Compute the leftmost, rightmost, topmost and bottom visible grid lines
        int bottom = (int) Math.ceil(visibleLR.getY() / interval);
        int top = (int) Math.floor(visibleUL.getY() / interval);
        int left = (int) Math.ceil(visibleUL.getX() / interval);
        int right = (int) Math.floor(visibleLR.getX() / interval);

        // Build the gridlines positions, store them as user-space coordinates,
        // screen-space coordinates, and as a drawable path (in screen-space)
        final int countMinH = Math.max(0, right - left + 1);
        linePositions[0] = new double[countMinH];
        linePositionsScreen[0] = new int[countMinH];

        final int countMinV = Math.max(0, top - bottom + 1);
        linePositions[1] = new double[countMinV];
        linePositionsScreen[1] = new int[countMinV];

        Point2D p = new Point2D.Double();
        Point2D pScreen = new Point2D.Double();
        for (int x = left; x <= right; x++) {
            linePositions[0][x - left] = x * interval;
            p.setLocation(x * interval, 0);
            viewport.getTransform().transform(p, pScreen);
            linePositionsScreen[0][x - left] = (int) pScreen.getX();
        }

        for (int y = bottom; y <= top; y++) {
            linePositions[1][y - bottom] = y * interval;
            p.setLocation(0, y * interval);
            viewport.getTransform().transform(p, pScreen);
            linePositionsScreen[1][y - bottom] = (int) pScreen.getY();
        }

        for (int x = left; x <= right; x++) {
            int xScreen = linePositionsScreen[0][x - left];
            for (int y = bottom; y <= top; y++) {
                int yScreen = linePositionsScreen[1][y - bottom];
                linesPath.moveTo(xScreen - crossRadius, yScreen);
                linesPath.lineTo(xScreen + crossRadius, yScreen);
                linesPath.moveTo(xScreen, yScreen - crossRadius);
                linesPath.lineTo(xScreen, yScreen + crossRadius);
            }
        }
    }

    private void updateGridMinorLines(Viewport viewport, double interval) {
        minorLinesPath = new Path2D.Double();
        updateGridLines(viewport, interval, minorLinePositions, minorLinePositionsScreen, minorLinesPath);
    }

    private void updateGridMajorLines(Viewport viewport, double interval) {
        majorLinesPath = new Path2D.Double();
        updateGridLines(viewport, interval, majorLinePositions, majorLinePositionsScreen, majorLinesPath);
    }

    private void updateGridLines(Viewport viewport, double interval,
            double[][] linePositions, int[][] linePositionsScreen, Path2D linesPath) {

        // Compute the visible user space area from the viewport
        Rectangle view = viewport.getShape();
        Point2D visibleUL = new Point2D.Double();
        Point2D visibleLR = new Point2D.Double();
        Point viewLL = new Point(view.x, view.height + view.y);
        Point viewUR = new Point(view.width + view.x, view.y);
        viewport.getInverseTransform().transform(viewLL, visibleUL);
        viewport.getInverseTransform().transform(viewUR, visibleLR);

        // Compute the leftmost, rightmost, topmost and bottom visible grid lines
        int bottom = (int) Math.ceil(visibleLR.getY() / interval);
        int top = (int) Math.floor(visibleUL.getY() / interval);
        int left = (int) Math.ceil(visibleUL.getX() / interval);
        int right = (int) Math.floor(visibleLR.getX() / interval);

        // Build the gridlines positions, store them as user-space coordinates,
        // screen-space coordinates, and as a drawable path (in screen-space)
        final int countMajH = Math.max(0, right - left + 1);
        linePositions[0] = new double[countMajH];
        linePositionsScreen[0] = new int[countMajH];

        final int countMajV = Math.max(0, top - bottom + 1);
        linePositions[1] = new double[countMajV];
        linePositionsScreen[1] = new int[countMajV];

        Point2D p = new Point2D.Double();
        Point2D pScreen = new Point2D.Double();
        for (int x = left; x <= right; x++) {
            linePositions[0][x - left] = x * interval;
            p.setLocation(x * interval, 0);
            viewport.getTransform().transform(p, pScreen);
            linePositionsScreen[0][x - left] = (int) pScreen.getX();
            linesPath.moveTo((int) pScreen.getX(), viewLL.getY());
            linesPath.lineTo((int) pScreen.getX(), viewUR.getY());
        }

        for (int y = bottom; y <= top; y++) {
            linePositions[1][y - bottom] = y * interval;
            p.setLocation(0, y * interval);
            viewport.getTransform().transform(p, pScreen);
            linePositionsScreen[1][y - bottom] = (int) pScreen.getY();

            linesPath.moveTo(viewLL.getX(), pScreen.getY());
            linesPath.lineTo(viewUR.getX(), pScreen.getY());
        }
    }

    /**
     * Draws the grid. <i>Note that this drawing procedure assumes that the Graphics2D object will draw in screen coordinates.</i>
     * Please restore the graphics transform object to the original state before calling this method.
     * @param g
     * Graphics2D object for the component the viewport is drawn onto.
     */
    public void draw(Graphics2D g) {
        g.setStroke(stroke);
        g.setColor(EditorCommonSettings.getGridColor());
        g.draw(minorLinesPath);
        g.setColor(EditorCommonSettings.getGridColor().darker());
        g.draw(majorLinesPath);
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
        return minorLinePositions;
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
        return majorLinePositions;
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
        return minorLinePositionsScreen;
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
     * Registers a new grid listener that will be notified if grid parameters change.
     * @param listener
     * The new listener.
     */
    public void addListener(GridListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     * @param listener
     * The listener to remove.
     */
    public void removeListener(GridListener listener) {
        listeners.remove(listener);
    }

    /**
     * Snap coordinate to the closest minor grid line.
     * @param x coordinate value
     * @return snapped coordinate value
     */
    public double snapCoordinate(double x) {
        double m = majorInterval * minorIntervalFactor;
        return Math.floor(x / m + 0.5) * m;
    }

}
