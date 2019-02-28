package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.ConnectionTool;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.circuit.utils.ConnectionUtils;

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
        ConnectionUtils.moveInternalContacts(connection);
        return connection;
    }

}
