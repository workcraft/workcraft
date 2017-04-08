package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;

/**
 * Main router class.
 */
public class Router {

    private final CoordinatesRegistryBuilder registryBuilder = new CoordinatesRegistryBuilder();
    private final RouterCellsBuilder cellsBuilder = new RouterCellsBuilder();
    private final AbstractRoutingAlgorithm algorithm = new DijkstraRouter();
    private RouterTask routerTask;
    private List<Route> routesFound;
    private CoordinatesRegistry coordinatesPhase2;

    public void setRouterTask(RouterTask routerTask) {
        if (routerTask == null || routerTask.equals(this.routerTask)) {
            return;
        }
        this.routerTask = routerTask;
        routeConnections();
    }

    public RouterTask getObstacles() {
        return routerTask;
    }

    public CoordinatesRegistry getCoordinatesRegistry() {
        return coordinatesPhase2;
    }

    public void routeConnections() {
        long start = System.currentTimeMillis();
        // 1st phase
        CoordinatesRegistry coordinatesPhase1 = registryBuilder.buildPhase1Coordinates(routerTask);
        coordinatesPhase1.setRouterCells(cellsBuilder.buildRouterCells(coordinatesPhase1, routerTask));

        routesFound = algorithm.route(routerTask, coordinatesPhase1, false);

        // 2nd phase
        UsageCounter usageCounter = algorithm.getUsageCounter();

        coordinatesPhase2 = registryBuilder.buildPhase2Coordinates(routerTask, coordinatesPhase1, usageCounter);
        coordinatesPhase2.setRouterCells(cellsBuilder.buildRouterCells(coordinatesPhase2, routerTask));
        routesFound = algorithm.route(routerTask, coordinatesPhase2, true);

        long stop = System.currentTimeMillis();
        System.out.println("Routing completed in " + (stop - start) + "ms");
    }

    public List<Route> getRoutingResult() {
        return routesFound;
    }

}
