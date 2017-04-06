package org.workcraft.plugins.circuit.routing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.CoordinateOrientation;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.PortDirection;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;
import org.workcraft.plugins.circuit.routing.impl.Route;
import org.workcraft.plugins.circuit.routing.impl.Router;
import org.workcraft.plugins.circuit.routing.impl.RouterCells;
import org.workcraft.plugins.circuit.routing.impl.RouterTask;

/**
 * The class creates the routing task and launches the router.
 */
public class RouterClient {

    private final Router router = new Router();
    private final Map<Contact, RouterPort> portMap = new HashMap<>();

    public void registerObstacles(VisualCircuit circuit) {
        portMap.clear();
        RouterTask newTask = new RouterTask();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            Rectangle internalBoundingBox = getRectangle(component.getInternalBoundingBox());
            newTask.addRectangle(internalBoundingBox);
            for (VisualContact contact : component.getContacts()) {
                Point portPoint = new Point(contact.getX() + component.getX(), contact.getY() + component.getY());
                RouterPort newPort = RouterPort.withFlexibleDirection(getDirection(contact), portPoint);
                portMap.put(contact.getReferencedContact(), newPort);
                newTask.addPort(newPort);
                Line portSegment = internalBoundingBox.getPortSegment(portPoint);
                newTask.addSegment(portSegment);
            }
        }

        for (VisualContact port : circuit.getVisualPorts()) {
            Rectangle2D internalBoundingBox = port.getInternalBoundingBox();
            newTask.addRectangle(getRectangle(internalBoundingBox));

            RouterPort newPort = RouterPort.withFixedDirection(getDirection(port),
                    new Point(internalBoundingBox.getCenterX(), internalBoundingBox.getCenterY()));

            portMap.put(port.getReferencedContact(), newPort);
            newTask.addPort(newPort);
        }

        for (Entry<Contact, RouterPort> entry : portMap.entrySet()) {
            Contact node = entry.getKey();
            RouterPort source = entry.getValue();

            for (Contact nodeDest : findDestinations(circuit.getMathModel(), node)) {
                RouterPort destination = portMap.get(nodeDest);
                newTask.addConnection(new RouterConnection(source, destination));
            }
        }
        router.setRouterTask(newTask);
    }

    private Set<Contact> findDestinations(Circuit circuit, MathNode source) {
        Set<Contact> collected = new HashSet<>();
        Set<Node> postSet = circuit.getPostset(source);
        for (Node node : postSet) {
            assert node instanceof MathNode;
            if (node instanceof Contact) {
                collected.add((Contact) node);
            } else {
                collected.addAll(findDestinations(circuit, (MathNode) node));
            }
        }
        return collected;
    }

    private PortDirection getDirection(VisualContact contact) {
        VisualContact.Direction direction = contact.getDirection();
        PortDirection converted = null;
        switch (direction) {
        case EAST:
            converted = PortDirection.EAST;
            break;
        case WEST:
            converted = PortDirection.WEST;
            break;
        case NORTH:
            converted = PortDirection.NORTH;
            break;
        case SOUTH:
            converted = PortDirection.SOUTH;
            break;
        default:
            assert false : "unsupported visual contact direction";
        }
        if (!(contact.getParent() instanceof VisualComponent)) {
            converted = converted.flip();
        }
        return converted;
    }

    private Rectangle getRectangle(Rectangle2D rect) {
        return new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public void draw(Graphics2D g, Viewport viewport) {
        // drawCoordinates(g, viewport);
        // drawBlocks(g);
        // drawSegments(g);
        // drawCells(g);
        // drawConnections(g);
        drawRoutes(g);
    }

    private void drawRoutes(Graphics2D g) {
        for (Route route : router.getRoutingResult()) {
            Path2D routeSegments = new Path2D.Double();
            routeSegments.moveTo(route.source.getLocation().getX(), route.source.getLocation().getY());
            if (!route.isRouteFound()) {
                g.setStroke(new BasicStroke(2.5f * (float) CircuitSettings.getBorderWidth()));
            } else {
                g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
            }
            for (Point routePoint : route.getPoints()) {
                routeSegments.lineTo(routePoint.getX(), routePoint.getY());
            }
            g.setColor(Color.RED);
            g.draw(routeSegments);
        }
    }

    private void drawConnections(Graphics2D g) {
        for (RouterConnection connection : router.getObstacles().getConnections()) {
            RouterPort src = connection.getSource();
            RouterPort dest = connection.getDestination();
            Line2D line = new Line2D.Double(src.getLocation().getX(), src.getLocation().getY(), dest.getLocation().getX(), dest.getLocation().getY());
            g.setColor(Color.RED);
            g.draw(line);
        }
    }

    private void drawCoordinates(Graphics2D g, Viewport viewport) {
        java.awt.Rectangle bounds = viewport.getShape();
        java.awt.Point screenTopLeft = new java.awt.Point(bounds.x, bounds.y);
        Point2D userTopLeft = viewport.screenToUser(screenTopLeft);
        java.awt.Point screenBottomRight = new java.awt.Point(bounds.x + bounds.width, bounds.y + bounds.height);
        Point2D userBottomRight = viewport.screenToUser(screenBottomRight);

        for (Coordinate x : router.getCoordinatesRegistry().getXCoordinates()) {
            if (!x.isPublic()) {
                continue;
            }

            Path2D grid = new Path2D.Double();
            grid.moveTo(x.getValue(), userTopLeft.getY());
            grid.lineTo(x.getValue(), userBottomRight.getY());
            g.setColor(Color.GRAY.brighter());

            if (x.getOrientation() == CoordinateOrientation.ORIENT_LOWER) {
                g.setColor(Color.BLUE);
            }
            if (x.getOrientation() == CoordinateOrientation.ORIENT_HIGHER) {
                g.setColor(Color.GREEN);
            }

            g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
            g.draw(grid);
        }
        for (Coordinate y : router.getCoordinatesRegistry().getYCoordinates()) {
            if (!y.isPublic()) {
                continue;
            }

            Path2D grid = new Path2D.Double();
            grid.moveTo(userTopLeft.getX(), y.getValue());
            grid.lineTo(userBottomRight.getX(), y.getValue());
            g.setColor(Color.GRAY.brighter());

            if (y.getOrientation() == CoordinateOrientation.ORIENT_LOWER) {
                g.setColor(Color.BLUE);
            }
            if (y.getOrientation() == CoordinateOrientation.ORIENT_HIGHER) {
                g.setColor(Color.GREEN);
            }
            g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
            g.draw(grid);
        }

    }

    private void drawCells(Graphics2D g) {
        RouterCells rcells = router.getCoordinatesRegistry().getRouterCells();

        int[][] cells = rcells.cells;

        int y = 0;
        for (Coordinate dy : router.getCoordinatesRegistry().getYCoordinates()) {
            int x = 0;
            for (Coordinate dx : router.getCoordinatesRegistry().getXCoordinates()) {
                boolean isBusy = (cells[x][y] & CellState.BUSY) > 0;
                boolean isVerticalPrivate = (cells[x][y] & CellState.VERTICAL_PUBLIC) == 0;
                boolean isHorizontalPrivate = (cells[x][y] & CellState.HORIZONTAL_PUBLIC) == 0;

                boolean isVerticalBlock = (cells[x][y] & CellState.VERTICAL_BLOCK) != 0;
                boolean isHorizontalBlock = (cells[x][y] & CellState.HORIZONTAL_BLOCK) != 0;

                Path2D shape = new Path2D.Double();

                if (isBusy) {
                    g.setColor(Color.RED);
                    shape.moveTo(dx.getValue() - 0.1, dy.getValue() - 0.1);
                    shape.lineTo(dx.getValue() + 0.1, dy.getValue() + 0.1);
                    shape.moveTo(dx.getValue() + 0.1, dy.getValue() - 0.1);
                    shape.lineTo(dx.getValue() - 0.1, dy.getValue() + 0.1);
                    g.draw(shape);
                } else {

                    if (isVerticalPrivate) {
                        shape = new Path2D.Double();
                        g.setColor(Color.MAGENTA.darker());
                        shape.moveTo(dx.getValue(), dy.getValue() - 0.1);
                        shape.lineTo(dx.getValue(), dy.getValue() + 0.1);
                        g.draw(shape);
                    }

                    if (isHorizontalPrivate) {
                        shape = new Path2D.Double();
                        g.setColor(Color.MAGENTA.darker());
                        shape.moveTo(dx.getValue() - 0.1, dy.getValue());
                        shape.lineTo(dx.getValue() + 0.1, dy.getValue());
                        g.draw(shape);
                    }

                    if (isVerticalBlock) {
                        shape = new Path2D.Double();
                        g.setColor(Color.RED);
                        shape.moveTo(dx.getValue(), dy.getValue() - 0.1);
                        shape.lineTo(dx.getValue(), dy.getValue() + 0.1);
                        g.draw(shape);
                    }

                    if (isHorizontalBlock) {
                        shape = new Path2D.Double();
                        g.setColor(Color.RED);
                        shape.moveTo(dx.getValue() - 0.1, dy.getValue());
                        shape.lineTo(dx.getValue() + 0.1, dy.getValue());
                        g.draw(shape);
                    }
                }
                x++;
            }
            y++;
        }
    }

    private void drawSegments(Graphics2D g) {
        g.setColor(Color.BLUE.darker());
        for (Line registeredSegment : router.getObstacles().getSegments()) {
            Path2D shape = new Path2D.Double();
            shape.moveTo(registeredSegment.getX1(), registeredSegment.getY1());
            shape.lineTo(registeredSegment.getX2(), registeredSegment.getY2());
            g.draw(shape);
        }
    }

    private void drawBlocks(Graphics2D g) {
        g.setColor(Color.BLUE.darker());
        for (Rectangle rec : router.getCoordinatesRegistry().blocked) {
            Rectangle2D drec = new Rectangle2D.Double(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());
            g.draw(drec);
        }
    }

}