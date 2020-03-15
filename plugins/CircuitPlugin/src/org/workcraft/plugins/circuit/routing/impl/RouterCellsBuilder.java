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

    public static RouterCells buildRouterCells(CoordinatesRegistry coordRegistry, RouterTask routerTask) {
        RouterCells routerCells = new RouterCells(
                coordRegistry.getXCoords().size(),
                coordRegistry.getYCoords().size());

        markPrivate(coordRegistry, routerCells);
        markBlocked(coordRegistry, routerCells, routerTask);
        markBusy(coordRegistry, routerCells, routerTask);
        return routerCells;
    }

    private static void markPrivate(CoordinatesRegistry coordRegistry, RouterCells routerCells) {
        if (routerCells.cells.length == 0) {
            return;
        }
        int xlen = routerCells.cells.length;
        int ylen = routerCells.cells[0].length;
        int x = 0;
        for (Coordinate dx: coordRegistry.getXCoords().getValues()) {
            if (dx.isAccessible()) {
                routerCells.unmark(x, 0, x, ylen - 1, CellState.VERTICAL_PRIVATE);
            } else {
                routerCells.mark(x, 0, x, ylen - 1, CellState.VERTICAL_PRIVATE);
            }
            x++;
        }
        int y = 0;
        for (Coordinate dy: coordRegistry.getYCoords().getValues()) {
            if (dy.isAccessible()) {
                routerCells.unmark(0, y, xlen - 1, y, CellState.HORIZONTAL_PRIVATE);
            } else {
                routerCells.mark(0, y, xlen - 1, y, CellState.HORIZONTAL_PRIVATE);
            }
            y++;
        }
    }

    private static void markBlocked(CoordinatesRegistry coordRegistry, RouterCells routerCells, RouterTask routerTask) {
        double snap = CircuitLayoutSettings.getSnappingMinor();
        for (RouterPort port: routerTask.getPorts()) {
            if (!port.isFixedDirection()) continue;

            Point p = port.getLocation();
            IndexedPoint ip = coordRegistry.getIndexedCoordinate(port.getLocation());
            IndexedCoordinates xCoords = coordRegistry.getXCoords();
            IndexedInterval xInterval = xCoords.getIndexedIntervalExclusive(p.getX() - snap, p.getX() + snap);
            IndexedCoordinates yCoords = coordRegistry.getYCoords();
            IndexedInterval yInterval = yCoords.getIndexedIntervalExclusive(p.getY() - snap, p.getY() + snap);

            if ((ip != null) && (xInterval != null) && (yInterval != null)) {
                if (port.getDirection().isHorizontal()) {
                    routerCells.mark(xInterval, yInterval, CellState.VERTICAL_BLOCK);
                } else {
                    routerCells.mark(xInterval, yInterval, CellState.HORIZONTAL_BLOCK);
                }
            }
        }
    }

    private static void markBusy(CoordinatesRegistry coordRegistry, RouterCells routerCells, RouterTask routerTask) {
        double margin = CircuitLayoutSettings.getMarginObstacleBusy();
        IndexedCoordinates xCoords = coordRegistry.getXCoords();
        IndexedCoordinates yCoords = coordRegistry.getYCoords();
        for (Rectangle rect: routerTask.getRectangles()) {
            IndexedInterval xInterval = xCoords.getIndexedIntervalExclusive(
                    rect.getX() - margin, rect.getX() + rect.getWidth() + margin);

            IndexedInterval yInterval = yCoords.getIndexedIntervalExclusive(
                    rect.getY() - margin, rect.getY() + rect.getHeight() + margin);

            routerCells.markBusy(xInterval, yInterval);
        }

        for (Line line: routerTask.getSegments()) {
            IndexedInterval xInt = xCoords.getIndexedInterval(line.getMinX(), line.getMaxX());
            IndexedInterval yInt = yCoords.getIndexedInterval(line.getMinY(), line.getMaxY());
            routerCells.markBusy(xInt, yInt);
        }
    }

}
