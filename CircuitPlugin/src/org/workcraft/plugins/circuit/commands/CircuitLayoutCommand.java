package org.workcraft.plugins.circuit.commands;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.gui.graph.commands.AbstractLayoutCommand;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.routing.RouterClient;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.impl.Route;
import org.workcraft.plugins.circuit.routing.impl.Router;
import org.workcraft.plugins.circuit.routing.impl.RouterTask;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CircuitLayoutCommand extends AbstractLayoutCommand {
    private static final double DX = 10;
    private static final double DY = 5;

    @Override
    public String getDisplayName() {
        return "Circuit placement and routing";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public void layout(VisualModel model) {
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            if (!skipLayoutPlacement()) {
                setComponentPosition(circuit);
                alignPorts(circuit);
            }
            setPolylineConnections(circuit, skipLayoutRouting());
            if (!skipLayoutRouting()) {
                routeWires(circuit);
            }
        }
    }

    public boolean skipLayoutPlacement() {
        return false;
    }

    public boolean skipLayoutRouting() {
        return false;
    }

    private void setComponentPosition(VisualCircuit circuit) {
        LinkedList<HashSet<VisualComponent>> layers = rankComponents(circuit);
        double x = (1.0 - layers.size()) * DX / 2.0;
        for (HashSet<VisualComponent> layer: layers) {
            double y = (1.0 - layer.size()) * DY / 2.0;
            for (VisualComponent component: layer) {
                Point2D pos = new Point2D.Double(x, y);
                component.setPosition(pos);
                if (component instanceof VisualCircuitComponent) {
                    VisualCircuitComponent circuitComponent = (VisualCircuitComponent) component;
                    setContactPositions(circuitComponent);
                }
                y += DY;
            }
            x += DX;
        }
    }

    private void setContactPositions(VisualCircuitComponent circuitComponent) {
        for (VisualContact contact: circuitComponent.getContacts()) {
            if (contact.isInput()) {
                contact.setPosition(new Point2D.Double(-1.0, 0.0));
            } else {
                contact.setPosition(new Point2D.Double(1.0, 0.0));
            }
        }
        circuitComponent.setContactsDefaultPosition();
    }

    private LinkedList<HashSet<VisualComponent>> rankComponents(VisualCircuit model) {
        LinkedList<HashSet<VisualComponent>> result = new LinkedList<>();

        HashSet<VisualComponent> inputPorts = new HashSet<>();
        for (VisualContact contact: Hierarchy.getDescendantsOfType(model.getRoot(), VisualContact.class)) {
            if (contact.isPort() && contact.isInput()) {
                inputPorts.add(contact);
            }
        }

        HashSet<VisualCircuitComponent> remainingComponents = new HashSet<>(Hierarchy.getDescendantsOfType(
                model.getRoot(), VisualCircuitComponent.class));

        HashSet<VisualComponent> currentLayer = inputPorts;
        HashSet<VisualComponent> firstLayer = null;
        while (!currentLayer.isEmpty()) {
            remainingComponents.removeAll(currentLayer);
            result.add(currentLayer);
            currentLayer = getNextLayer(model, currentLayer);
            currentLayer.retainAll(remainingComponents);
            if (firstLayer == null) {
                firstLayer = currentLayer;
            }
        }
        if (firstLayer == null) {
            firstLayer = new HashSet<>();
        }
        firstLayer.addAll(remainingComponents);
        if ((result.size() < 2) && !firstLayer.isEmpty()) {
            result.add(firstLayer);
        }

        HashSet<VisualComponent> outputPorts = new HashSet<>();
        for (VisualContact contact: Hierarchy.getDescendantsOfType(model.getRoot(), VisualContact.class)) {
            if (contact.isPort() && contact.isOutput()) {
                outputPorts.add(contact);
            }
        }
        result.add(outputPorts);
        return result;
    }

    private HashSet<VisualComponent> getNextLayer(final VisualCircuit model, HashSet<VisualComponent> layer) {
        HashSet<VisualComponent> result = new HashSet<>();
        for (VisualComponent component: layer) {
            result.addAll(CircuitUtils.getComponentPostset(model, component));
        }
        return result;
    }

    private void setPolylineConnections(VisualCircuit circuit, boolean routeSelfLoops) {
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class)) {
            connection.setConnectionType(ConnectionType.POLYLINE);
            ConnectionGraphic graphic = connection.getGraphic();
            graphic.setDefaultControlPoints();
            if (routeSelfLoops) {
                routeSelfLoop(connection);
            }
        }
    }

    private void routeSelfLoop(VisualConnection connection) {
        VisualNode firstNode = connection.getFirst();
        VisualNode secondNode = connection.getSecond();
        if ((firstNode instanceof VisualContact) && (secondNode instanceof VisualContact)) {
            VisualContact firstContact = (VisualContact) firstNode;
            VisualContact secondContact = (VisualContact) secondNode;
            if (!firstContact.isPort() && !secondContact.isPort()
                    && (firstContact.getParent() == secondContact.getParent())) {
                Point2D firstPos = firstContact.getRootSpacePosition();
                Point2D secondPos = secondContact.getRootSpacePosition();
                Node parent = firstContact.getParent();
                double h = 2.0;
                if (parent instanceof VisualCircuitComponent) {
                    VisualCircuitComponent component = (VisualCircuitComponent) parent;
                    Rectangle2D bb = component.getInternalBoundingBoxInLocalSpace();
                    h = bb.getHeight();
                }
                double d = firstPos.getY() - secondPos.getY();
                double dx = 1.0 - Math.abs(d);
                if (dx < 0.0) dx = 0.0;
                double dy = (d > 0) ? h - d : -h - d;
                ConnectionGraphic graphic = connection.getGraphic();
                if (graphic instanceof Polyline) {
                    Polyline polyline = (Polyline) graphic;
                    polyline.addControlPoint(new Point2D.Double(firstPos.getX(), firstPos.getY() - dy));
                    polyline.addControlPoint(new Point2D.Double(secondPos.getX() - dx, firstPos.getY() - dy));
                    polyline.addControlPoint(new Point2D.Double(secondPos.getX() - dx, secondPos.getY()));
                }
            }
        }
    }

    private void alignPorts(VisualCircuit circuit) {
        for (VisualContact contact: circuit.getVisualPorts()) {
            if (contact.isOutput()) {
                VisualContact driver = CircuitUtils.findDriver(circuit, contact);
                if (driver != null) {
                    contact.setRootSpaceY(driver.getRootSpaceY());
                }
            }
            if (contact.isInput()) {
                double y = 0.0;
                int count = 0;
                for (VisualContact driven: CircuitUtils.findDriven(circuit, contact)) {
                    y += driven.getRootSpaceY();
                    count++;
                }
                if (count > 0) {
                    contact.setRootSpaceY(y / (double) count);
                }
            }
        }
    }

    private void routeWires(VisualCircuit circuit) {
        Router router = new Router();
        RouterClient routingClient = new RouterClient();
        RouterTask routerTask = routingClient.registerObstacles(circuit);
        router.setRouterTask(routerTask);
        for (Route route: router.getRoutingResult()) {
            VisualContact srcContact = routingClient.getContact(route.source);
            VisualContact dstContact = routingClient.getContact(route.destination);
            Connection connection = circuit.getConnection(srcContact, dstContact);
            if (connection instanceof VisualConnection) {
                List<Point2D> locationsInRootSpace = new ArrayList<>();
                for (Point routePoint : route.getPoints()) {
                    locationsInRootSpace.add(new Point2D.Double(routePoint.getX(), routePoint.getY()));
                }
                ConnectionHelper.addControlPoints((VisualConnection) connection, locationsInRootSpace);
            }
        }
        mergeConnections(circuit);
    }

    private void mergeConnections(VisualCircuit circuit) {
        boolean done;
        do {
            HashMap<Node, HashSet<VisualConnection>> srcToConnetions = new HashMap<>();
            for (VisualConnection connection: Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class)) {
                Node src = connection.getFirst();
                HashSet<VisualConnection> connections = srcToConnetions.get(src);
                if (connections == null) {
                    connections = new HashSet<>();
                    srcToConnetions.put(src, connections);
                }
                connections.add(connection);
            }
            done = true;
            for (HashSet<VisualConnection> connections: srcToConnetions.values()) {
                done &= mergeConnections(circuit, connections);
            }
        } while (!done);
    }

    private boolean mergeConnections(VisualCircuit circuit, HashSet<VisualConnection> connections) {
        return true;
    }

}
