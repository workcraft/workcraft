package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;

/**
 * Main router class.
 */
public class Router {

    private final CoordinatesRegistryBuilder registryBuilder = new CoordinatesRegistryBuilder();
    private final AbstractRoutingAlgorithm algorithm = new DijkstraRouter();
    private RouterTask routerTask = null;
    private List<Route> routesFound = null;
    private CoordinatesRegistry coordPhase = null;

    public void routeConnections(RouterTask routerTask) {
        if (routerTask == null || routerTask.equals(this.routerTask)) {
            return;
        }
        this.routerTask = routerTask;
        routeConnectionsPhase1();
        routeConnectionsPhase2();
    }

    private void routeConnectionsPhase1() {
        coordPhase = registryBuilder.buildPhase1Coordinates(routerTask);
        RouterCells routerCells = RouterCellsBuilder.buildRouterCells(coordPhase, routerTask);
        coordPhase.setRouterCells(routerCells);
        routesFound = algorithm.route(routerTask, coordPhase, false);
    }

    private void routeConnectionsPhase2() {
        UsageCounter usageCounter = algorithm.getUsageCounter();
        coordPhase = registryBuilder.buildPhase2Coordinates(routerTask, coordPhase, usageCounter);
        RouterCells routerCells = RouterCellsBuilder.buildRouterCells(coordPhase, routerTask);
        coordPhase.setRouterCells(routerCells);
        routesFound = algorithm.route(routerTask, coordPhase, true);
    }

    public RouterTask getObstacles() {
        return routerTask;
    }

    public CoordinatesRegistry getCoordinatesRegistry() {
        return coordPhase;
    }

    public List<Route> getRoutingResult() {
        return routesFound;
    }

}
