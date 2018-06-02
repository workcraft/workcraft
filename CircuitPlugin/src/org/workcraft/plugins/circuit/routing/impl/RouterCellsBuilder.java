package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.commands.CircuitLayoutSettings;
import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.IndexedInterval;
import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;

/**
 * Service class producing {@link RouterCells} from given
 * {@link CoordinatesRegistry} and {@link RouterTask}.
 */
public class RouterCellsBuilder {

    public RouterCells buildRouterCells(CoordinatesRegistry coordinatesRegistry, RouterTask routerTask) {
        RouterCells routerCells = new RouterCells(
                coordinatesRegistry.getXCoords().size(),
                coordinatesRegistry.getYCoords().size());

        markVerticalPublic(coordinatesRegistry, routerCells);
        markHorizontalPublic(coordinatesRegistry, routerCells);

        markBusy(coordinatesRegistry, routerCells, routerTask);
        markBlocked(coordinatesRegistry, routerCells, routerTask);

        return routerCells;
    }

    private void markBlocked(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells, RouterTask routerTask) {
        double snapMinor = CircuitLayoutSettings.getSnappingMinor();
        for (RouterPort port: routerTask.getPorts()) {
            Point location = port.getLocation();
            IndexedPoint ip = coordinatesRegistry.getIndexedCoordinate(port.getLocation());
            IndexedInterval xInterval = coordinatesRegistry.getXCoords().getIndexedIntervalExclusive(
                    location.getX() - snapMinor, location.getX() + snapMinor);
            IndexedInterval yInterval = coordinatesRegistry.getYCoords().getIndexedIntervalExclusive(
                    location.getY() - snapMinor, location.getY() + snapMinor);

            if (ip != null && xInterval != null && yInterval != null) {
                if (port.getDirection().isVertical()) {
                    routerCells.mark(xInterval.getFrom(), yInterval.getFrom(), xInterval.getTo(), yInterval.getTo(),
                            CellState.HORIZONTAL_BLOCK);
                } else {
                    routerCells.mark(xInterval.getFrom(), yInterval.getFrom(), xInterval.getTo(), yInterval.getTo(),
                            CellState.VERTICAL_BLOCK);
                }
            }
        }

        for (Line segment: routerTask.getSegments()) {
            markBlockedSegment(coordinatesRegistry, routerCells, segment, null);
        }
    }

    public void markBlockedSegment(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells,
            Line segment, RouterPort sourcePort) {

        double x1 = Math.min(segment.getX1(), segment.getX2());
        double x2 = Math.max(segment.getX1(), segment.getX2());
        double y1 = Math.min(segment.getY1(), segment.getY2());
        double y2 = Math.max(segment.getY1(), segment.getY2());
        double margin = CircuitLayoutSettings.getMarginSegment();
        IndexedCoordinates xCoords = coordinatesRegistry.getXCoords();
        IndexedCoordinates yCoords = coordinatesRegistry.getYCoords();

        IndexedInterval xInclusive = xCoords.getIndexedInterval(x1, x2);
        IndexedInterval yInclusive = yCoords.getIndexedInterval(y1, y2);
        IndexedInterval xWithMarginInclusive = xCoords.getIndexedInterval(x1 - margin, x2 + margin);
        IndexedInterval yWithMarginInclusive = yCoords.getIndexedInterval(y1 - margin, y2 + margin);
        IndexedInterval xWithMarginExclusive = xCoords.getIndexedIntervalExclusive(x1 - margin, x2 + margin);
        IndexedInterval yWithMarginExclusive = yCoords.getIndexedIntervalExclusive(y1 - margin, y2 + margin);

        if (segment.isVertical()) {
            routerCells.mark(xWithMarginExclusive, yWithMarginExclusive, CellState.VERTICAL_BLOCK);
            routerCells.unmark(xInclusive, yInclusive, CellState.VERTICAL_BLOCK);
            routerCells.unmark(xWithMarginInclusive, yWithMarginInclusive, CellState.VERTICAL_PUBLIC);
            routerCells.markSourcePorts(xInclusive, yInclusive, sourcePort);
        }

        if (segment.isHorizontal()) {
            routerCells.mark(xWithMarginExclusive, yWithMarginExclusive, CellState.HORIZONTAL_BLOCK);
            routerCells.unmark(xInclusive, yInclusive, CellState.HORIZONTAL_BLOCK);
            routerCells.unmark(xWithMarginInclusive, yWithMarginInclusive, CellState.HORIZONTAL_PUBLIC);
            routerCells.markSourcePorts(xInclusive, yInclusive, sourcePort);
        }
    }

    private void markBusy(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells, RouterTask routerTask) {
        double marginBusy = CircuitLayoutSettings.getMarginObstacleBusy();
        for (Rectangle rectangle: routerTask.getRectangles()) {
            IndexedInterval xInt = coordinatesRegistry.getXCoords().getIndexedIntervalExclusive(
                    rectangle.getX() - marginBusy, rectangle.getX() + rectangle.getWidth() + marginBusy);

            IndexedInterval yInt = coordinatesRegistry.getYCoords().getIndexedIntervalExclusive(
                    rectangle.getY() - marginBusy, rectangle.getY() + rectangle.getHeight() + marginBusy);

            routerCells.markBusy(xInt, yInt);
        }
    }

    private void markVerticalPublic(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells) {
        if (routerCells.cells.length == 0) {
            return;
        }
        int ylen = routerCells.cells[0].length;
        int x = 0;
        for (Coordinate dx: coordinatesRegistry.getXCoords().getValues()) {
            if (dx.isPublic()) {
                routerCells.mark(x, 0, x, ylen - 1, CellState.VERTICAL_PUBLIC);
            }
            x++;
        }
    }

    private void markHorizontalPublic(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells) {
        int xlen = routerCells.cells.length;
        int y = 0;
        for (Coordinate dy: coordinatesRegistry.getYCoords().getValues()) {
            if (dy.isPublic()) {
                routerCells.mark(0, y, xlen - 1, y, CellState.HORIZONTAL_PUBLIC);
            }
            y++;
        }
    }

}
