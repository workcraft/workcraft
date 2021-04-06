package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.ConnectionTool;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.circuit.utils.ConnectionUtils;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CircuitConnectionTool extends ConnectionTool {

    @Override
    public boolean isConnectable(Node node) {
        if (node instanceof VisualContact) {
            VisualContact contact = (VisualContact) node;
            return (firstNode == null) == contact.isDriver();
        }
        return (node instanceof VisualJoint)
                || (node instanceof VisualCircuitComponent)
                || (node instanceof VisualCircuitConnection);
    }

    @Override
    public VisualConnection createTemplateNode() {
        VisualCircuitConnection result = new VisualCircuitConnection();
        result.setArrowLength(0.0);
        return result;
    }

    @Override
    public ContinuousConnectionMode getContinuousConnectionMode() {
        return ContinuousConnectionMode.FAN;
    }

    @Override
    public VisualConnection finishConnection(GraphEditorMouseEvent e) {
        VisualConnection connection = super.finishConnection(e);
        ConnectionUtils.moveInternalContacts(connection);

        // Adjust position of a newly created joint
        VisualNode fromNode = connection.getFirst();
        if ((firstNode instanceof VisualConnection) && (fromNode instanceof VisualJoint)) {
            VisualJoint joint = (VisualJoint) fromNode;
            VisualTransformableNode adjacentNode = getConnectionSecondTransformableNode(connection);
            if (adjacentNode != null) {
                Set<Point2D> snaps = Collections.singleton(adjacentNode.getRootSpacePosition());
                Point2D snapPos = e.getEditor().snap(joint.getRootSpacePosition(), snaps);
                joint.setRootSpacePosition(snapPos);
            }
        }
        return connection;
    }

    private VisualTransformableNode getConnectionSecondTransformableNode(VisualConnection connection) {
        if (connection != null) {
            ConnectionGraphic graphic = connection.getGraphic();
            if (graphic instanceof Polyline) {
                List<ControlPoint> controlPoints = graphic.getControlPoints();
                if (!controlPoints.isEmpty()) {
                    return controlPoints.iterator().next();
                }
            }
            VisualNode toNode = connection.getSecond();
            if (toNode instanceof VisualTransformableNode) {
                return (VisualTransformableNode) toNode;
            }
        }
        return null;
    }

}
