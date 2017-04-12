package org.workcraft.plugins.circuit.routing;

import java.awt.geom.Rectangle2D;
import java.util.Map.Entry;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.PortDirection;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;
import org.workcraft.plugins.circuit.routing.impl.RouterTask;
import org.workcraft.util.TwoWayMap;

/**
 * The class creates the routing task and keeps association of VisualContacts with RouterPorts.
 */
public class RouterClient {

    private final TwoWayMap<VisualContact, RouterPort> contactToRouterPortMap = new TwoWayMap<>();

    public RouterTask registerObstacles(VisualCircuit circuit) {
        contactToRouterPortMap.clear();
        RouterTask routerTask = new RouterTask();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            Rectangle2D bb = component.getInternalBoundingBox();
            Rectangle internalBoundingBox = new Rectangle(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight());
            routerTask.addRectangle(internalBoundingBox);
            for (VisualContact contact : component.getContacts()) {
                Point pos = new Point(contact.getX() + component.getX(), contact.getY() + component.getY());
                RouterPort routerPort = new RouterPort(getDirection(contact), pos, false);
                contactToRouterPortMap.put(contact, routerPort);
                routerTask.addPort(routerPort);
                Line portSegment = internalBoundingBox.getPortSegment(pos);
                routerTask.addSegment(portSegment);
            }
        }

        for (VisualContact port : circuit.getVisualPorts()) {
            Rectangle2D bb = port.getInternalBoundingBox();
            routerTask.addRectangle(new Rectangle(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight()));
            Point pos = new Point(bb.getCenterX(), bb.getCenterY());
            RouterPort routerPort = new RouterPort(getDirection(port), pos, true);
            contactToRouterPortMap.put(port, routerPort);
            routerTask.addPort(routerPort);
        }

        for (Entry<VisualContact, RouterPort> entry : contactToRouterPortMap.entrySet()) {
            VisualContact srcContact = entry.getKey();
            if (srcContact.isDriven()) continue;
            RouterPort srcRouterPort = entry.getValue();
            for (VisualContact dstContact : CircuitUtils.findDriven(circuit, srcContact, false)) {
                if (srcContact == dstContact) continue;
                RouterPort dstRouterPort = getRouterPort(dstContact);
                routerTask.addConnection(new RouterConnection(srcRouterPort, dstRouterPort));
            }
        }
        return routerTask;
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

    public VisualContact getContact(RouterPort routerPort) {
        return contactToRouterPortMap.getKey(routerPort);
    }

    public RouterPort getRouterPort(VisualContact contact) {
        return contactToRouterPortMap.getValue(contact);
    }

}