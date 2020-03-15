package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.commands.CircuitLayoutSettings;
import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.CoordinateOrientation;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;

/**
 * Service object building {@link CoordinatesRegistry} for phase1 and for phase2
 * router.
 */
public class CoordinatesRegistryBuilder {

    public CoordinatesRegistry buildPhase1Coordinates(RouterTask routerTask) {
        CoordinatesRegistry baseRegistry = new CoordinatesRegistry();
        registerBoundaries(baseRegistry, routerTask);
        registerRectangles(baseRegistry, routerTask);
        registerPorts(baseRegistry, routerTask);
        registerObstacleCoordinates(baseRegistry, routerTask);
        return baseRegistry;
    }

    public CoordinatesRegistry buildPhase2Coordinates(RouterTask routerTask,
            CoordinatesRegistry otherRegistry, UsageCounter usageCounter) {

        CoordinatesRegistry baseRegistry = new CoordinatesRegistry();
        for (int x = 0; x < usageCounter.getWidth(); x++) {
            int xUsage = usageCounter.getXCoordUsage(x);
            Coordinate xCoord = otherRegistry.getXCoords().getCoordinateByIndex(x);
            baseRegistry.getXCoords().addCoordinate(xCoord, xUsage);
        }

        for (int y = 0; y < usageCounter.getHeight(); y++) {
            int yUsage = usageCounter.getYCoordUsage(y);
            Coordinate yCoord = otherRegistry.getYCoords().getCoordinateByIndex(y);
            baseRegistry.getYCoords().addCoordinate(yCoord, yUsage);
        }
        registerBoundaries(baseRegistry, routerTask);
        registerRectangles(baseRegistry, routerTask);
        registerPorts(baseRegistry, routerTask);
        registerObstacleCoordinates(baseRegistry, routerTask);
        return baseRegistry;
    }

    private void registerBoundaries(CoordinatesRegistry baseRegistry, RouterTask routerTask) {
        if (!routerTask.getRectangles().isEmpty()) {
            Rectangle first = routerTask.getRectangles().iterator().next();

            double xMin = first.getX();
            double xMax = first.getX() + first.getWidth();
            double yMin = first.getY();
            double yMax = first.getY() + first.getHeight();

            for (Rectangle rec: routerTask.getRectangles()) {
                xMin = Math.min(xMin, rec.getX());
                yMin = Math.min(yMin, rec.getY());
                xMax = Math.max(xMax, rec.getX() + rec.getWidth());
                yMax = Math.max(yMax, rec.getY() + rec.getHeight());
            }

            for (RouterPort port: routerTask.getPorts()) {
                xMin = Math.min(xMin, port.getLocation().getX());
                yMin = Math.min(yMin, port.getLocation().getY());
                xMax = Math.max(xMax, port.getLocation().getX());
                yMax = Math.max(yMax, port.getLocation().getY());
            }
            registerSnappedRectangle(baseRegistry, new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin));
        }
    }

    private void registerObstacleCoordinates(CoordinatesRegistry baseRegistry, RouterTask routerTask) {
        for (Rectangle rec: routerTask.getRectangles()) {
            IndexedCoordinates xCoords = baseRegistry.getXCoords();
            boolean foundHorizontal = xCoords.isIntervalOccupied(rec.getX(), rec.getX() + rec.getWidth());
            if (!foundHorizontal) {
                xCoords.addPrivate(CoordinateOrientation.ORIENT_NONE, rec.getX() + rec.getWidth() / 2);
            }

            IndexedCoordinates yCoords = baseRegistry.getYCoords();
            boolean foundVertical = yCoords.isIntervalOccupied(rec.getY(), rec.getY() + rec.getHeight());
            if (!foundVertical) {
                yCoords.addPrivate(CoordinateOrientation.ORIENT_NONE, rec.getY() + rec.getHeight() / 2);
            }
        }
    }

    private void registerPorts(CoordinatesRegistry baseRegistry, RouterTask routerTask) {
        for (RouterPort port: routerTask.getPorts()) {
            baseRegistry.registerPort(port);
        }
    }

    private void registerRectangles(CoordinatesRegistry baseRegistry, RouterTask routerTask) {
        for (Rectangle rec: routerTask.getRectangles()) {
            registerSnappedRectangle(baseRegistry, rec);
        }
    }

    private void registerSnappedRectangle(CoordinatesRegistry baseRegistry, Rectangle rec) {
        double margin = CircuitLayoutSettings.getMarginObstacle();
        double snap = CircuitLayoutSettings.getSnappingMajor();

        double xMin = SnapHelper.snapToLower(rec.getX() - margin, snap);
        baseRegistry.getXCoords().addPublic(CoordinateOrientation.ORIENT_LOWER, xMin);

        double xMax = SnapHelper.snapToHigher(rec.getX() + rec.getWidth() + margin, snap);
        baseRegistry.getXCoords().addPublic(CoordinateOrientation.ORIENT_HIGHER, xMax);

        double yMin = SnapHelper.snapToLower(rec.getY() - margin, snap);
        baseRegistry.getYCoords().addPublic(CoordinateOrientation.ORIENT_LOWER, yMin);

        double yMax = SnapHelper.snapToHigher(rec.getY() + rec.getHeight() + margin, snap);
        baseRegistry.getYCoords().addPublic(CoordinateOrientation.ORIENT_HIGHER, yMax);
    }

}
