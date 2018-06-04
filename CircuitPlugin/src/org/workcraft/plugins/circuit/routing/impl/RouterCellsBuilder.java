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

    public static RouterCells buildRouterCells(CoordinatesRegistry coordinatesRegistry, RouterTask routerTask) {
        RouterCells routerCells = new RouterCells(
                coordinatesRegistry.getXCoords().size(),
                coordinatesRegistry.getYCoords().size());

        markVerticalPublic(coordinatesRegistry, routerCells);
        markHorizontalPublic(coordinatesRegistry, routerCells);
        markBlocked(coordinatesRegistry, routerCells, routerTask);
        markBusy(coordinatesRegistry, routerCells, routerTask);
        return routerCells;
    }

    private static void markVerticalPublic(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells) {
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

    private static void markHorizontalPublic(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells) {
        int xlen = routerCells.cells.length;
        int y = 0;
        for (Coordinate dy: coordinatesRegistry.getYCoords().getValues()) {
            if (dy.isPublic()) {
                routerCells.mark(0, y, xlen - 1, y, CellState.HORIZONTAL_PUBLIC);
            }
            y++;
        }
    }

    private static void markBlocked(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells, RouterTask routerTask) {
        double snapMinor = CircuitLayoutSettings.getSnappingMinor();
        for (RouterPort port: routerTask.getPorts()) {
            if (!port.isFixedDirection()) continue;

            Point location = port.getLocation();
            IndexedPoint ip = coordinatesRegistry.getIndexedCoordinate(port.getLocation());
            IndexedInterval xInterval = coordinatesRegistry.getXCoords().getIndexedIntervalExclusive(
                    location.getX() - snapMinor, location.getX() + snapMinor);
            IndexedInterval yInterval = coordinatesRegistry.getYCoords().getIndexedIntervalExclusive(
                    location.getY() - snapMinor, location.getY() + snapMinor);

            if (ip != null && xInterval != null && yInterval != null) {
                if (port.getDirection().isHorizontal()) {
                    routerCells.mark(xInterval, yInterval, CellState.VERTICAL_BLOCK);
                } else {
                    routerCells.mark(xInterval, yInterval, CellState.HORIZONTAL_BLOCK);
                }
            }
        }
    }

    private static void markBusy(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells, RouterTask routerTask) {
        double margin = CircuitLayoutSettings.getMarginObstacleBusy();
        for (Rectangle rectangle: routerTask.getRectangles()) {
            IndexedInterval xInt = coordinatesRegistry.getXCoords().getIndexedIntervalExclusive(
                    rectangle.getX() - margin, rectangle.getX() + rectangle.getWidth() + margin);

            IndexedInterval yInt = coordinatesRegistry.getYCoords().getIndexedIntervalExclusive(
                    rectangle.getY() - margin, rectangle.getY() + rectangle.getHeight() + margin);

            routerCells.markBusy(xInt, yInt);
        }

        for (Line line: routerTask.getSegments()) {
            IndexedInterval xInt = coordinatesRegistry.getXCoords().getIndexedInterval(line.getMinX(), line.getMaxX());
            IndexedInterval yInt = coordinatesRegistry.getYCoords().getIndexedInterval(line.getMinY(), line.getMaxY());
            routerCells.markBusy(xInt, yInt);
        }
    }

}
