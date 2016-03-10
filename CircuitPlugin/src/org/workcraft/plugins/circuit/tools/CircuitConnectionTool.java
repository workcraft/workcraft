package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;

public class CircuitConnectionTool extends ConnectionTool {

    @Override
    public boolean isConnectable(Node node) {
        return (node instanceof VisualContact)
              || (node instanceof VisualJoint)
              || (node instanceof VisualCircuitConnection);
    }

    @Override
    public VisualConnection createDefaultTemplateNode() {
        VisualCircuitConnection result = new VisualCircuitConnection();
        result.setArrowLength(0.0);
        return result;
    }

    @Override
    public ContinuousConnectionMode getContinuousConnectionMode() {
        return ContinuousConnectionMode.FAN;
    }

}
