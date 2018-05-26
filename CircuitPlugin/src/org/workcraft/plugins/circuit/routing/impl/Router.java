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
    private CoordinatesRegistry coordinatesPhase = null;

    public void routeConnections(RouterTask routerTask) {
        if (routerTask == null || routerTask.equals(this.routerTask)) {
            return;
        }
        this.routerTask = routerTask;
        // 1st phase
        coordinatesPhase = registryBuilder.buildPhase1Coordinates(routerTask);
        coordinatesPhase.setRouterCells(cellsBuilder.buildRouterCells(coordinatesPhase, routerTask));
        routesFound = algorithm.route(routerTask, coordinatesPhase, false);
        // 2nd phase
        UsageCounter usageCounter = algorithm.getUsageCounter();
        coordinatesPhase = registryBuilder.buildPhase2Coordinates(routerTask, coordinatesPhase, usageCounter);
        coordinatesPhase.setRouterCells(cellsBuilder.buildRouterCells(coordinatesPhase, routerTask));
        routesFound = algorithm.route(routerTask, coordinatesPhase, true);
    }

    public RouterTask getObstacles() {
        return routerTask;
    }

    public CoordinatesRegistry getCoordinatesRegistry() {
        return coordinatesPhase;
    }

    public List<Route> getRoutingResult() {
        return routesFound;
    }

}
