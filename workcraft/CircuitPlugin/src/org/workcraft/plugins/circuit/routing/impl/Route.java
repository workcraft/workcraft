package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;

public class Route {
    public boolean found;
    public final RouterPort source;
    public final RouterPort destination;
    private final List<Point> points = new ArrayList<>();

    public Route(RouterPort source, RouterPort destination) {
        this.source = source;
        this.destination = destination;
    }

    public void clear() {
        points.clear();
    }

    public void add(Point point) {
        points.add(point);
    }

    public void setRouteFound(boolean isFound) {
        found = isFound;
    }

    public boolean isRouteFound() {
        return found;
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return "Route [points=" + points + "]";
    }

}
