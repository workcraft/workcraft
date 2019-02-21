package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;

import java.awt.geom.Point2D;

public class CircuitConnectionTool extends ConnectionTool {

    @Override
    public boolean isConnectable(Node node) {
        return (node instanceof VisualContact)
              || (node instanceof VisualJoint)
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
        if (connection != null) {
            ConnectionGraphic graphic = connection.getGraphic();
            if (graphic instanceof Polyline) {
                Polyline polyline = (Polyline) graphic;
                if (polyline.getControlPointCount() > 0) {
                    ControlPoint firstControlPoint = polyline.getFirstControlPoint();
                    Point2D firstPos = firstControlPoint.getRootSpacePosition();
                    ControlPoint lastControlPoint = polyline.getLastControlPoint();
                    Point2D lastPos = lastControlPoint.getRootSpacePosition();
                    if (moveContactIfInsideComponent(connection.getFirst(), firstPos)) {
                        polyline.remove(firstControlPoint);
                    }
                    if (moveContactIfInsideComponent(connection.getSecond(), lastPos)) {
                        polyline.remove(lastControlPoint);
                    }
                }
            }
        }
        return connection;
    }

    private boolean moveContactIfInsideComponent(VisualNode node, Point2D pos) {
        if (node instanceof VisualContact) {
            VisualContact contact = (VisualContact) node;
            Node parent = contact.getParent();
            if (parent instanceof VisualCircuitComponent) {
                VisualCircuitComponent component = (VisualCircuitComponent) parent;
                if (component.getInternalBoundingBoxInLocalSpace().contains(contact.getPosition())) {
                    contact.setRootSpacePosition(pos);
                    return true;
                }
            }
        }
        return false;
    }

}
