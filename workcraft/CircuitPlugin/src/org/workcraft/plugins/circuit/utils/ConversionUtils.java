package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.circuit.VisualReplicaContact;
import org.workcraft.plugins.circuit.commands.DissolveJointTransformationCommand;
import org.workcraft.utils.ModelUtils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

public class ConversionUtils {

    public static void collapseReplicaContact(VisualCircuit circuit, VisualReplicaContact replica) {
        VisualComponent firstNode = replica.getMaster();
        Point2D replicaPositionInRootSpace = replica.getRootSpacePosition();
        Container container = (Container) replica.getParent();
        Set<VisualConnection> outgoingConnections = circuit.getConnections(replica).stream()
                .filter(c -> c.getFirst() == replica).collect(Collectors.toSet());

        if (outgoingConnections.size() > 1) {
            VisualJoint joint = circuit.createJoint(container);
            joint.setRootSpacePosition(replicaPositionInRootSpace);
            try {
                circuit.connect(firstNode, joint);
            } catch (InvalidConnectionException ignored) {
            }
            firstNode = joint;
        }

        for (VisualConnection outgoingConnection : outgoingConnections) {
            VisualNode secondNode = outgoingConnection.getSecond();
            LinkedList<Point2D> locationsInRootSpace
                    = ConnectionHelper.getSuffixControlPoints(outgoingConnection, replicaPositionInRootSpace);

            if (!(firstNode instanceof VisualJoint)) {
                locationsInRootSpace.addFirst(replicaPositionInRootSpace);
            }
            circuit.remove(outgoingConnection);
            try {
                VisualConnection newConnection = circuit.connect(firstNode, secondNode);
                ConnectionHelper.addControlPoints(newConnection, locationsInRootSpace);
            } catch (InvalidConnectionException ignored) {
            }
        }
    }

    public static VisualConnection replicateDriverContact(VisualCircuit circuit, VisualContact drivenContact) {
        Set<VisualConnection> connections = circuit.getConnections(drivenContact);
        if (connections.size() == 1) {
            VisualConnection connection = connections.iterator().next();
            if (connection.getSecond() == drivenContact) {
                VisualContact driverContact = CircuitUtils.findDriver(circuit, drivenContact, false);

                Container container = drivenContact.isPort()
                        ? (Container) drivenContact.getParent()
                        : (Container) drivenContact.getParent().getParent();

                VisualReplicaContact replicaDriverContact
                        = circuit.createVisualReplica(driverContact, VisualReplicaContact.class, container);

                Point2D pos = drivenContact.getRootSpacePosition();
                VisualContact.Direction direction = drivenContact.getDirection();
                int sign = drivenContact.isPort() ? -1 : 1;
                ModelUtils.refreshBoundingBox(circuit, replicaDriverContact);
                Rectangle2D replicaBox = replicaDriverContact.getBoundingBoxInLocalSpace();
                double xOffset = sign * direction.getGradientX() * (0.5 + 0.5 * replicaBox.getWidth());
                double yOffset = sign * direction.getGradientY() * (0.5 + 0.5 * replicaBox.getHeight());
                replicaDriverContact.setRootSpacePosition(new Point2D.Double(pos.getX() + xOffset, pos.getY() + yOffset));

                VisualNode firstNode = connection.getFirst();
                circuit.remove(connection);
                if (firstNode instanceof VisualJoint) {
                    int size = circuit.getConnections(firstNode).size();
                    if (size < 2) {
                        circuit.remove(firstNode);
                    } else if (size == 2) {
                        new DissolveJointTransformationCommand().transformNodes(circuit, Collections.singleton(firstNode));
                    }
                }

                try {
                    return circuit.connect(replicaDriverContact, drivenContact);
                } catch (InvalidConnectionException ignored) {
                }
            }
        }
        return null;
    }

}
