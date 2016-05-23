/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import org.workcraft.plugins.shared.CommonEditorSettings;

/**
 * The <code>Grid</code> class is used to generate and draw the background grid, guidelines,
 * as well as to handle the coordinate "snapping".
 * @author Ivan Poliakov
 *
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

    protected Color majorLinesColor = new Color(200, 200, 200);
    protected Color minorLinesColor = new Color(240, 240, 240);
    protected Color guideLinesColor = Color.RED;

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
     * @return
     * The major grid lines drawing color.
     */
    public Color getMajorLinesColor() {
        return majorLinesColor;
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
        return minorLinesColor;
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

        listeners = new LinkedList<GridListener>();

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

        if (CommonEditorSettings.getLightGrid()) {
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
        Rectangle view = viewport.getShape();

        // Compute the visible user space area from the viewport
        Point2D visibleUL = new Point2D.Double();
        Point2D visibleLR = new Point2D.Double();
        Point viewLL = new Point(view.x, view.height + view.y);
        Point viewUR = new Point(view.width + view.x, view.y);
        viewport.getInverseTransform().transform(viewLL, visibleUL);
        viewport.getInverseTransform().transform(viewUR, visibleLR);

        int left = (int) Math.ceil(visibleUL.getX() / interval);
        int right = (int) Math.floor(visibleLR.getX() / interval);
        int bottom = (int) Math.ceil(visibleLR.getY() / interval);
        int top = (int) Math.floor(visibleUL.getY() / interval);

        // Build the gridlines positions, store them as user-space coordinates,
        // screen-space coordinates, and as a drawable path (in screen-space)
        minorLinesPath = new Path2D.Double();
        Point2D p = new Point2D.Double();
        Point2D pScreen = new Point2D.Double();

        final int countMinH = Math.max(0, right - left + 1);
        minorLinePositions[0] = new double[countMinH];
        minorLinePositionsScreen[0] = new int[countMinH];

        final int countMinV = Math.max(0, top - bottom + 1);
        minorLinePositions[1] = new double[countMinV];
        minorLinePositionsScreen[1] = new int[countMinV];

        for (int x = left; x <= right; x++) {
            minorLinePositions[0][x - left] = x * interval;
            p.setLocation(x * interval, 0);
            viewport.getTransform().transform(p, pScreen);
            minorLinePositionsScreen[0][x - left] = (int) pScreen.getX();
        }

        for (int y = bottom; y <= top; y++) {
            minorLinePositions[1][y - bottom] = y * interval;
            p.setLocation(0, y * interval);
            viewport.getTransform().transform(p, pScreen);
            minorLinePositionsScreen[1][y - bottom] = (int) pScreen.getY();
        }

        for (int x = left; x <= right; x++) {
            int xScreen = minorLinePositionsScreen[0][x - left];
            for (int y = bottom; y <= top; y++) {
                int yScreen = minorLinePositionsScreen[1][y - bottom];
                minorLinesPath.moveTo(xScreen - 1, yScreen);
                minorLinesPath.lineTo(xScreen + 1, yScreen);
                minorLinesPath.moveTo(xScreen, yScreen - 1);
                minorLinesPath.lineTo(xScreen, yScreen + 1);
            }
        }
    }

    private void updateGridMinorLines(Viewport viewport, double interval) {
        Rectangle view = viewport.getShape();

        // Compute the visible user space area from the viewport
        Point2D visibleUL = new Point2D.Double();
        Point2D visibleLR = new Point2D.Double();
        Point viewLL = new Point(view.x, view.height + view.y);
        Point viewUR = new Point(view.width + view.x, view.y);
        viewport.getInverseTransform().transform(viewLL, visibleUL);
        viewport.getInverseTransform().transform(viewUR, visibleLR);

        int left = (int) Math.ceil(visibleUL.getX() / interval);
        int right = (int) Math.floor(visibleLR.getX() / interval);
        int bottom = (int) Math.ceil(visibleLR.getY() / interval);
        int top = (int) Math.floor(visibleUL.getY() / interval);

        // Build the gridlines positions, store them as user-space coordinates,
        // screen-space coordinates, and as a drawable path (in screen-space)
        minorLinesPath = new Path2D.Double();
        Point2D p = new Point2D.Double();
        Point2D pScreen = new Point2D.Double();

        final int countMinH = Math.max(0, right - left + 1);
        minorLinePositions[0] = new double[countMinH];
        minorLinePositionsScreen[0] = new int[countMinH];

        final int countMinV = Math.max(0, top - bottom + 1);
        minorLinePositions[1] = new double[countMinV];
        minorLinePositionsScreen[1] = new int[countMinV];

        for (int x = left; x <= right; x++) {
            minorLinePositions[0][x - left] = x * interval;
            p.setLocation(x * interval, 0);
            viewport.getTransform().transform(p, pScreen);
            minorLinePositionsScreen[0][x - left] = (int) pScreen.getX();

            minorLinesPath.moveTo(pScreen.getX(), viewLL.getY());
            minorLinesPath.lineTo(pScreen.getX(), viewUR.getY());
        }

        for (int y = bottom; y <= top; y++) {
            minorLinePositions[1][y - bottom] = y * interval;
            p.setLocation(0, y * interval);
            viewport.getTransform().transform(p, pScreen);
            minorLinePositionsScreen[1][y - bottom] = (int) pScreen.getY();

            minorLinesPath.moveTo(viewLL.getX(), pScreen.getY());
            minorLinesPath.lineTo(viewUR.getX(), pScreen.getY());
        }
    }

    private void updateGridMajorCrosses(Viewport viewport, double interval) {
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
        majorLinesPath = new Path2D.Double();
        Point2D p = new Point2D.Double();
        Point2D pScreen = new Point2D.Double();

        final int countMajH = Math.max(0, right - left + 1);
        majorLinePositions[0] = new double[countMajH];
        majorLinePositionsScreen[0] = new int[countMajH];

        final int countMajV = Math.max(0, top - bottom + 1);
        majorLinePositions[1] = new double[countMajV];
        majorLinePositionsScreen[1] = new int[countMajV];

        for (int x = left; x <= right; x++) {
            majorLinePositions[0][x - left] = x * interval;
            p.setLocation(x * interval, 0);
            viewport.getTransform().transform(p, pScreen);
            majorLinePositionsScreen[0][x - left] = (int) pScreen.getX();
        }

        for (int y = bottom; y <= top; y++) {
            majorLinePositions[1][y - bottom] = y * interval;
            p.setLocation(0, y * interval);
            viewport.getTransform().transform(p, pScreen);
            majorLinePositionsScreen[1][y - bottom] = (int) pScreen.getY();
        }

        for (int x = left; x <= right; x++) {
            int xScreen = majorLinePositionsScreen[0][x - left];
            for (int y = bottom; y <= top; y++) {
                int yScreen = majorLinePositionsScreen[1][y - bottom];
                majorLinesPath.moveTo(xScreen - 1, yScreen);
                majorLinesPath.lineTo(xScreen + 1, yScreen);
                majorLinesPath.moveTo(xScreen, yScreen - 1);
                majorLinesPath.lineTo(xScreen, yScreen + 1);
            }
        }
    }

    private void updateGridMajorLines(Viewport viewport, double interval) {
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
        majorLinesPath = new Path2D.Double();

        final int countMajH = Math.max(0, right - left + 1);
        majorLinePositions[0] = new double[countMajH];
        majorLinePositionsScreen[0] = new int[countMajH];

        final int countMajV = Math.max(0, top - bottom + 1);
        majorLinePositions[1] = new double[countMajV];
        majorLinePositionsScreen[1] = new int[countMajV];

        Point2D p = new Point2D.Double();
        Point2D pScreen = new Point2D.Double();

        for (int x = left; x <= right; x++) {
            majorLinePositions[0][x - left] = x * majorInterval;
            p.setLocation(x * majorInterval, 0);
            viewport.getTransform().transform(p, pScreen);
            majorLinePositionsScreen[0][x - left] = (int) pScreen.getX();
            majorLinesPath.moveTo((int) pScreen.getX(), viewLL.getY());
            majorLinesPath.lineTo((int) pScreen.getX(), viewUR.getY());
        }

        for (int y = bottom; y <= top; y++) {
            majorLinePositions[1][y - bottom] = y * majorInterval;
            p.setLocation(0, y * majorInterval);
            viewport.getTransform().transform(p, pScreen);
            majorLinePositionsScreen[1][y - bottom] = (int) pScreen.getY();

            majorLinesPath.moveTo(viewLL.getX(), pScreen.getY());
            majorLinesPath.lineTo(viewUR.getX(), pScreen.getY());
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
        g.setColor(minorLinesColor);
        g.draw(minorLinesPath);
        g.setColor(majorLinesColor);
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
