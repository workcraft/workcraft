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

    private static final int MAX_GRID_COUNT = 256;
    private static final double MINOR_INTERVAL_FACTOR = 0.1;
    private static final double INTERVAL_SCALE_FACTOR = 2;
    private static final double INTERVAL_MIN_SCREEN_MM = 50.0;
    private static final double INTERVAL_MAX_SCREEN_MM = 100.0;
    public static final double INTERVAL_START_VALUE = 10.0;

    private final double[][] majorPositions = new double[2][];
    private final double[][] minorPositions = new double[2][];
    private final int[][] majorScreenPositions = new int[2][];
    private final int[][] minorScreenPositions = new int[2][];
    private final Stroke stroke = new BasicStroke();

    private double majorInterval = INTERVAL_START_VALUE;
    private Path2D minorShape = new Path2D.Double();
    private Path2D majorShape = new Path2D.Double();

    /**
     * The list of listeners to be notified in case of grid parameters change.
     */
    private final LinkedList<GridListener> listeners = new LinkedList<>();

    /**
     * Recalculates visible grid lines based on the viewport parameters.
     *
     * @param viewport The viewport to calculate gridlines for.
     */
    public void updateGrid(Viewport viewport) {
        Rectangle view = viewport.getShape();
        if (view.isEmpty()) return;

        updateMajorInterval(viewport);

        if (EditorCommonSettings.getLightGrid()) {
            updateGridMinorCrosses(viewport, majorInterval * MINOR_INTERVAL_FACTOR);
            updateGridMajorCrosses(viewport, majorInterval);
        } else {
            updateGridMinorLines(viewport, majorInterval * MINOR_INTERVAL_FACTOR);
            updateGridMajorLines(viewport, majorInterval);
        }

        for (GridListener l : listeners) {
            l.gridChanged(this);
        }
    }

    private void updateMajorInterval(Viewport viewport) {
        double scale = viewport.getTransform().getScaleX();
        if (scale > 0) {
            majorInterval = INTERVAL_START_VALUE;
            double minInterval = INTERVAL_MIN_SCREEN_MM * SizeHelper.getScreenDpmm() / scale;
            while (majorInterval < minInterval) {
                majorInterval *= INTERVAL_SCALE_FACTOR;
            }
            double maxInterval = INTERVAL_MAX_SCREEN_MM * SizeHelper.getScreenDpmm() / scale;
            while (majorInterval > maxInterval) {
                majorInterval /= INTERVAL_SCALE_FACTOR;
            }
        }
    }

    private void updateGridMinorCrosses(Viewport viewport, double interval) {
        minorShape = new Path2D.Double();
        double radius = Math.max(1.0, EditorCommonSettings.getFontSize() * SizeHelper.getScreenDpmm() / 30.0);
        updateGridCrosses(viewport, interval, radius, minorPositions, minorScreenPositions, minorShape);
    }

    private void updateGridMajorCrosses(Viewport viewport, double interval) {
        majorShape = new Path2D.Double();
        double radius = Math.max(1.0, EditorCommonSettings.getFontSize() * SizeHelper.getScreenDpmm() / 20.0);
        updateGridCrosses(viewport, interval, radius, majorPositions, majorScreenPositions, majorShape);
    }

    private void updateGridCrosses(Viewport viewport, double interval, double crossRadius,
            double[][] positions, int[][] screenPositions, Path2D shape) {

        updateGridPositions(viewport, interval, positions, screenPositions);

        for (int xIndex = 0; xIndex < screenPositions[0].length; xIndex++) {
            int xScreen = screenPositions[0][xIndex];
            for (int yIndex = 0; yIndex < screenPositions[1].length; yIndex++) {
                int yScreen = screenPositions[1][yIndex];
                shape.moveTo(xScreen - crossRadius, yScreen);
                shape.lineTo(xScreen + crossRadius, yScreen);
                shape.moveTo(xScreen, yScreen - crossRadius);
                shape.lineTo(xScreen, yScreen + crossRadius);
            }
        }
    }

    private void updateGridMinorLines(Viewport viewport, double interval) {
        minorShape = new Path2D.Double();
        updateGridLines(viewport, interval, minorPositions, minorScreenPositions, minorShape);
    }

    private void updateGridMajorLines(Viewport viewport, double interval) {
        majorShape = new Path2D.Double();
        updateGridLines(viewport, interval, majorPositions, majorScreenPositions, majorShape);
    }

    private void updateGridLines(Viewport viewport, double interval,
            double[][] positions, int[][] screenPositions, Path2D shape) {

        updateGridPositions(viewport, interval, positions, screenPositions);

        Rectangle view = viewport.getShape();
        for (int xIndex = 0; xIndex < screenPositions[0].length; xIndex++) {
            int xScreen = screenPositions[0][xIndex];
            shape.moveTo(xScreen, view.y + view.height);
            shape.lineTo(xScreen, view.y);
        }

        for (int yIndex = 0; yIndex < screenPositions[1].length; yIndex++) {
            int yScreen = screenPositions[1][yIndex];
            shape.moveTo(view.x, yScreen);
            shape.lineTo(view.x + view.width, yScreen);
        }
    }

    private Rectangle getGridBounds(Viewport viewport, double interval) {
        Rectangle view = viewport.getShape();

        Point viewLL = new Point(view.x, view.height + view.y);
        Point2D visibleUL = new Point2D.Double();
        viewport.getInverseTransform().transform(viewLL, visibleUL);

        Point viewUR = new Point(view.width + view.x, view.y);
        Point2D visibleLR = new Point2D.Double();
        viewport.getInverseTransform().transform(viewUR, visibleLR);

        // Compute the leftmost, rightmost, topmost and bottom visible grid lines
        int bottom = (int) Math.ceil(visibleLR.getY() / interval);
        int top = (int) Math.floor(visibleUL.getY() / interval);
        int left = (int) Math.ceil(visibleUL.getX() / interval);
        int right = (int) Math.floor(visibleLR.getX() / interval);
        return new Rectangle(left, bottom, right - left, top - bottom);
    }

    private void updateGridPositions(Viewport viewport, double interval,
            double[][] positions, int[][] screenPositions) {

        Point2D p = new Point2D.Double();
        Point2D pScreen = new Point2D.Double();
        Rectangle r = getGridBounds(viewport, interval);

        // Build the gridlines positions, store them as user-space coordinates,
        // screen-space coordinates, and as a drawable path (in screen-space)
        int xCount = Math.max(0, r.width + 1);
        int yCount = Math.max(0, r.height + 1);
        if ((xCount > MAX_GRID_COUNT) || (yCount > MAX_GRID_COUNT)) {
            xCount = 0;
            yCount = 0;
        }

        positions[0] = new double[xCount];
        screenPositions[0] = new int[xCount];
        for (int xIndex = 0; xIndex < xCount; xIndex++) {
            p.setLocation((r.x + xIndex) * interval, 0);
            positions[0][xIndex] = p.getX();
            viewport.getTransform().transform(p, pScreen);
            screenPositions[0][xIndex] = (int) pScreen.getX();
        }

        positions[1] = new double[yCount];
        screenPositions[1] = new int[yCount];
        for (int yIndex = 0; yIndex < yCount; yIndex++) {
            p.setLocation(0, (r.y + yIndex) * interval);
            positions[1][yIndex] = p.getY();
            viewport.getTransform().transform(p, pScreen);
            screenPositions[1][yIndex] = (int) pScreen.getY();
        }
    }

    /**
     * Draws the grid. <i>Note that this drawing procedure assumes that the Graphics2D object will draw in screen coordinates.</i>
     * Please restore the graphics transform object to the original state before calling this method.
     *
     * @param g Graphics2D object for the component the viewport is drawn onto.
     */
    public void draw(Graphics2D g) {
        g.setStroke(stroke);
        g.setColor(EditorCommonSettings.getGridColor());
        g.draw(minorShape);
        g.setColor(EditorCommonSettings.getGridColor().darker());
        g.draw(majorShape);
    }

    /**
     * Returns major grid lines positions <i>in user space, double precision</i> as a 2-dimensional array. First row of the array contains x-coordinates of the vertical grid lines,
     * second row contains y-coordinates of the horizontal grid lines.
     *
     * @return getMajorPositions()[0] - the array containing vertical grid lines positions
     * getMajorPositions()[1] - the array containing horizontal grid lines positions
     */
    public double[][] getMajorPositions() {
        return majorPositions;
    }

    /**
     * Returns minor grid lines positions <i>in screen space space, integer precision</i> as a 2-dimensional array.
     * First row of the array contains x-coordinates of the vertical grid lines, second row contains y-coordinates of the horizontal grid lines,
     *
     * @return getMinorScreenPositions()[0] - the array containing vertical grid lines positions
     * getMinorScreenPositions()[1] - the array containing horizontal grid lines positions
     */
    public int[][] getMinorScreenPositions() {
        return minorScreenPositions;
    }

    /**
     * Returns major grid lines positions <i>in screen space space, integer precision</i> as a 2-dimensional array. First row of the array contains Y-coordinates of the horizontal grid lines,
     * second row contains X-coordinates of the vertical grid lines.
     *
     * @return getMajorScreenPositions()[0] - the array containing vertical grid lines positions
     * getMajorScreenPositions()[1] - the array containing horizontal grid lines positions
     */
    public int[][] getMajorScreenPositions() {
        return majorScreenPositions;
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
     *
     * @param listener The new listener.
     */
    public void addListener(GridListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener The listener to remove.
     */
    public void removeListener(GridListener listener) {
        listeners.remove(listener);
    }

    /**
     * Snap coordinate to the closest minor grid line.
     * @param value coordinate value
     * @param subdivideCount number of subdivisions pet interval
     * @return snapped coordinate value
     */
    public double snapCoordinate(double value, int subdivideCount) {
        double m = majorInterval * MINOR_INTERVAL_FACTOR / (double) subdivideCount;
        return Math.floor(value / m + 0.5) * m;
    }

}
