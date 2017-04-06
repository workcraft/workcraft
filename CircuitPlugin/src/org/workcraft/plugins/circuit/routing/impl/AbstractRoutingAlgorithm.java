package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;

public abstract class AbstractRoutingAlgorithm {

    private final RouterCellsBuilder cellsBuilder = new RouterCellsBuilder();

    protected CellAnalyser analyser;
    protected RouterTask task;
    protected CoordinatesRegistry coordinates;

    private UsageCounter usageCounter;

    protected int width;
    protected int height;

    public List<Route> route(RouterTask task, CoordinatesRegistry coordinates, boolean occupyCells) {

        width = coordinates.getXCoordinates().size();
        height = coordinates.getYCoordinates().size();

        this.task = task;

        this.coordinates = coordinates;

        analyser = new CellAnalyser(coordinates);

        List<Route> routes = new ArrayList<>();
        List<List<IndexedPoint>> paths = new ArrayList<List<IndexedPoint>>();

        for (RouterConnection connection : task.getConnections()) {
            IndexedPoint sourcePoint = coordinates.getIndexedCoordinate(connection.getSource().getLocation());
            IndexedPoint destinationPoint = coordinates.getIndexedCoordinate(connection.getDestination().getLocation());

            analyser.initialise(connection);

            List<IndexedPoint> path = findRoute(sourcePoint, destinationPoint);

            Route route = new Route(connection.getSource(), connection.getDestination());

            if (path != null) {

                path = getCleanPath(path);
                paths.add(path);
                augmentRouteSegments(route, path);
                route.setRouteFound(true);

                if (occupyCells) {
                    markBlockedCells(route, coordinates);
                }
            } else {
                route.setRouteFound(false);
                route.add(connection.getSource().getLocation());
                route.add(connection.getDestination().getLocation());
            }

            routes.add(route);
        }

        usageCounter = new UsageCounter(width, height);
        usageCounter.updateUsageCounter(paths);


        // for (int x = 0; x < width; x++) {
        // System.out.print(usageCounter.getXCoordUsage(x) + " ");
        // }
        // System.out.println();

        return routes;
    }

    private void markBlockedCells(Route route, CoordinatesRegistry registry) {
        RouterCells cells = registry.getRouterCells();
        for (int i = 1; i < route.getPoints().size(); i++) {
            Point from = route.getPoints().get(i - 1);
            Point to = route.getPoints().get(i);
            Line segment = new Line(from.getX(), from.getY(), to.getX(), to.getY());

            cellsBuilder.markBlockedSegment(registry, cells, segment, route.source);
        }
    }

    /**
     * Augment route with path information.
     *
     * @param path
     *            the list of indexed points representing the path
     * @return route with path information added
     */
    protected Route augmentRouteSegments(Route route, List<IndexedPoint> path) {
        for (IndexedPoint point : path) {
            route.add(coordinates.getPoint(point.getX(), point.getY()));
        }
        return route;
    }

    /**
     * Remove points not forming route corners or end-points.
     *
     * @param path
     *            list of indexed points forming the route segments
     * @return new list of points without points in the middle of route segments
     */
    protected List<IndexedPoint> getCleanPath(List<IndexedPoint> path) {

        assert path.size() >= 2;

        List<IndexedPoint> cleanPath = new ArrayList<>();

        cleanPath.add(path.get(0));

        for (int i = 1; i < path.size() - 1; i++) {
            if (!isLineFormed(path.get(i - 1), path.get(i), path.get(i + 1))) {
                cleanPath.add(path.get(i));
            }
        }

        cleanPath.add(path.get(path.size() - 1));

        return cleanPath;
    }

    private boolean isLineFormed(IndexedPoint p1, IndexedPoint p2, IndexedPoint p3) {
        assert !p1.equals(p2) && !p2.equals(p3) && !p3.equals(p1);

        if (p1.getX() == p2.getX() && p2.getX() == p3.getX()) {
            return true;
        }

        if (p1.getY() == p2.getY() && p2.getY() == p3.getY()) {
            return true;
        }

        return false;
    }

    /**
     * from the given graph and the end-points, find the route path.
     *
     * @return list of indexed route coordinates
     */
    protected List<IndexedPoint> buildPath(IndexedPoint source, IndexedPoint[][] sourceCells) {
        List<IndexedPoint> path = new ArrayList<IndexedPoint>();
        path.add(source);

        IndexedPoint next = source;
        do {
            next = sourceCells[next.getX()][next.getY()];

            if (next != null) {
                path.add(next);
            }
        } while (next != null);

        if (path.size() < 2) {
            return null;
        }

        return path;
    }

    protected abstract List<IndexedPoint> findRoute(IndexedPoint source, IndexedPoint destination);

    protected UsageCounter getUsageCounter() {
        return usageCounter;
    }

}
