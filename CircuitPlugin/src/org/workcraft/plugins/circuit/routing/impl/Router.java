package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;

/**
 * Main router class.
 */
public class Router {

    private final CoordinatesRegistryBuilder registryBuilder = new CoordinatesRegistryBuilder();
    private final RouterCellsBuilder cellsBuilder = new RouterCellsBuilder();
    private final AbstractRoutingAlgorithm algorithm = new DijkstraRouter();
    private RouterTask routerTask = null;
    private List<Route> routesFound = null;
    private CoordinatesRegistry coordinatesPhase2 = null;

    public void routeConnections(RouterTask routerTask) {
        if (routerTask == null || routerTask.equals(this.routerTask)) {
            return;
        }
        this.routerTask = routerTask;
        // 1st phase
        CoordinatesRegistry coordinatesPhase1 = registryBuilder.buildPhase1Coordinates(routerTask);
        coordinatesPhase1.setRouterCells(cellsBuilder.buildRouterCells(coordinatesPhase1, routerTask));
        routesFound = algorithm.route(routerTask, coordinatesPhase1, false);

        // 2nd phase
        UsageCounter usageCounter = algorithm.getUsageCounter();
        coordinatesPhase2 = registryBuilder.buildPhase2Coordinates(routerTask, coordinatesPhase1, usageCounter);
        coordinatesPhase2.setRouterCells(cellsBuilder.buildRouterCells(coordinatesPhase2, routerTask));
        routesFound = algorithm.route(routerTask, coordinatesPhase2, true);
    }

    public RouterTask getObstacles() {
        return routerTask;
    }

    public CoordinatesRegistry getCoordinatesRegistry() {
        return coordinatesPhase2;
    }

    public List<Route> getRoutingResult() {
        return routesFound;
    }

}
