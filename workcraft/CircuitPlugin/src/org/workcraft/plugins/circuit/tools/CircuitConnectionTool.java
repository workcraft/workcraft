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
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.ConnectionUtils;
import org.workcraft.plugins.circuit.utils.ConversionUtils;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

public class CircuitConnectionTool extends ConnectionTool {

    public CircuitConnectionTool() {
        super(false, true, true);
    }

    @Override
    public boolean isConnectable(Node node) {
        if (node instanceof VisualContact contact) {
            return (firstNode == null) == contact.isDriver();
        }
        return ((node instanceof VisualReplicaContact) && (firstNode == null))
                || (node instanceof VisualJoint)
                || (node instanceof VisualConnection)
                || ((node instanceof VisualFunctionComponent) && ((VisualFunctionComponent) node).isBlackbox());
    }

    @Override
    public String getSecondHintMessage() {
        return super.getSecondHintMessage() + " Hold Shift to create contact proxy.";
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
        if (connection != null) {
            // Adjust position of adjacent contacts that are inside component box
            ConnectionUtils.adjustInsideComponentContactPositions(connection);
            VisualNode fromNode = connection.getFirst();
            if ((firstNode instanceof VisualConnection) && (fromNode instanceof VisualJoint)) {
                // Adjust position of a newly created joint
                GraphEditor editor = e.getEditor();
                VisualTransformableNode adjacentNode = getConnectionSecondTransformableNode(connection);
                if (adjacentNode != null) {
                    Point2D jointPosition = ((VisualJoint) fromNode).getRootSpacePosition();
                    Point2D adjacentNodePosition = adjacentNode.getRootSpacePosition();
                    Point2D snapPos = editor.snap(jointPosition, Collections.singleton(adjacentNodePosition));
                    ((VisualJoint) fromNode).setRootSpacePosition(snapPos);
                }
            }
            if (e.isShiftKeyDown()) {
                VisualCircuit circuit = (VisualCircuit) e.getEditor().getModel();
                VisualNode toNode = connection.getSecond();
                if (toNode instanceof VisualContact drivenContact) {
                    connection = ConversionUtils.replicateDriverContact(circuit, drivenContact);
                }
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
