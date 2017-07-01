package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.PortDirection;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;

/**
 * For given cells and requested movement this class returns movement cost or
 * whether the movement is event possible.
 */
public class CellAnalyser {
    private final RouterCells cells;

    private final int sizeX;
    private final int sizeY;

    private final CoordinatesRegistry coordinatesRegistry;

    private IndexedPoint sourcePoint;
    private RouterPort sourcePort;
    private PortDirection sourceDirection;
    private IndexedPoint destinationPoint;
    private PortDirection destinationDirection;

    public CellAnalyser(CoordinatesRegistry coordinatesRegistry) {
        this.coordinatesRegistry = coordinatesRegistry;
        cells = coordinatesRegistry.getRouterCells();

        sizeX = cells.cells.length;
        sizeY = (cells.cells.length > 0) ? cells.cells[0].length : 0;
    }

    public boolean isMovementPossible(int x, int y, int dx, int dy) {
        assert sourcePoint != null && destinationPoint != null : "source and destination must be known";
        assert dx != 0 || dy != 0 : "Has to move in x or y direction";
        assert dx == 0 || dy == 0 : "Diagonal movement is not allowed";
        assert Math.abs(dx) < 2 && Math.abs(dy) < 2 : "The movement length over more than 1 cell is not allowed";

        final int targetX = x + dx;
        final int targetY = y + dy;

        final boolean isOutsideBoundaries = targetX < 0 || targetX >= sizeX || targetY < 0 || targetY >= sizeY;

        if (isOutsideBoundaries) {
            return false;
        }

        if (dx != 0) {
            if (isSameHorizontalSource(x, y) && isSameHorizontalSource(targetX, targetY)) {
                return true;
            }
        }

        if (dy != 0) {
            if (isSameVerticalSource(x, y) && isSameVerticalSource(targetX, targetY)) {
                return true;
            }
        }

        if (targetX == sourcePoint.getX() && targetY == sourcePoint.getY()) {
            if (sourceDirection != null) {
                if (dx != -sourceDirection.getDx() || dy != -sourceDirection.getDy()) {
                    return false;
                }
            }
        }

        if (x == destinationPoint.getX() && y == destinationPoint.getY()) {
            if (destinationDirection != null) {
                if (dx != destinationDirection.getDx() || dy != destinationDirection.getDy()) {
                    return false;
                }
            }
        }

        if (dx != 0) {
            if (isForeignHorizontalSource(targetX, targetY)) {
                return false;
            }

            if (isSameHorizontalSource(x, y) && !isSameHorizontalSource(targetX, targetY)) {
                return false;
            }

            if (isBlockedHorizontally(x, y)) {
                return false;
            }

            if (isBlockedHorizontally(targetX, targetY)) {
                return false;
            }
        }

        if (dy != 0) {
            if (isForeignVerticalSource(targetX, targetY)) {
                return false;
            }

            if (isSameVerticalSource(x, y) && !isSameVerticalSource(targetX, targetY)) {
                return false;
            }

            if (isBlockedVertically(x, y)) {
                return false;
            }

            if (isBlockedVertically(targetX, targetY)) {
                return false;
            }
        }

        return true;
    }

    private boolean isForeignVerticalSource(int x, int y) {
        return cells.getVerticalSourcePort(x, y) != null && !sourcePort.equals(cells.getVerticalSourcePort(x, y));
    }

    private boolean isForeignHorizontalSource(int x, int y) {
        return cells.getHorizontalSourcePort(x, y) != null && !sourcePort.equals(cells.getHorizontalSourcePort(x, y));
    }

    private boolean isSameVerticalSource(int x, int y) {
        return sourcePort.equals(cells.getVerticalSourcePort(x, y));
    }

    private boolean isSameHorizontalSource(int x, int y) {
        return sourcePort.equals(cells.getHorizontalSourcePort(x, y));
    }

    private boolean isBlockedHorizontally(int x, int y) {
        if (isSameHorizontalSource(x, y)) {
            return false;
        }

        boolean isBlocked = cells.isMarked(x, y, CellState.HORIZONTAL_BLOCK);
        boolean isPrivate = y != destinationPoint.getY() && y != sourcePoint.getY()
                && !cells.isMarked(x, y, CellState.HORIZONTAL_PUBLIC);
        return isBlocked || isPrivate;
    }

    private boolean isBlockedVertically(int x, int y) {
        if (isSameVerticalSource(x, y)) {
            return false;
        }
        boolean isBlocked = cells.isMarked(x, y, CellState.VERTICAL_BLOCK);
        boolean isPrivate = x != destinationPoint.getX() && x != sourcePoint.getX()
                && !cells.isMarked(x, y, CellState.VERTICAL_PUBLIC);
        return isBlocked || isPrivate;
    }

    public Double getMovementCost(int lastX, int lastY, int x, int y, int dx, int dy) {
        if (!isMovementPossible(x, y, dx, dy)) {
            return null;
        }

        double cost = 0;

        int targetX = x + dx;
        int targetY = y + dy;
        if (cells.isMarked(targetX, targetY, CellState.BUSY)) {
            cost += 1000.0;
        }

        final boolean hasTurned = (x - lastX) != dx || (y - lastY) != dy;

        if (hasTurned) {
            cost += 3.0;
        }

        if (!isBlockedHorizontally(lastX, lastY) && isBlockedHorizontally(x, y)) {
            cost += 3.0;
        }

        if (!isBlockedVertically(lastX, lastY) && isBlockedVertically(x, y)) {
            cost += 3.0;
        }

        cost += coordinatesRegistry.getXCoords().getDistance(x, targetX);
        cost += coordinatesRegistry.getYCoords().getDistance(y, targetY);

        return cost + 1.0;
    }

    public double getHeuristicsCost(int x, int y) {
        return 0;
    }

    public void initialise(RouterConnection connection) {
        sourcePoint = coordinatesRegistry.getIndexedCoordinate(connection.getSource().getLocation());
        destinationPoint = coordinatesRegistry.getIndexedCoordinate(connection.getDestination().getLocation());

        sourceDirection = null;
        destinationDirection = null;

        if (connection.getSource().isFixedDirection()) {
            sourceDirection = connection.getSource().getDirection();
        }

        if (connection.getDestination().isFixedDirection()) {
            destinationDirection = connection.getDestination().getDirection();
        }

        sourcePort = connection.getSource();
    }

}
