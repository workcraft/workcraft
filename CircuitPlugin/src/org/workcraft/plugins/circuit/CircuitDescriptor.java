package org.workcraft.plugins.circuit;

import org.workcraft.dom.ModelDescriptor;

public class CircuitDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Digital Circuit";
    }

    @Override
    public Circuit createMathModel() {
        return new Circuit();
    }

    @Override
    public VisualCircuitDescriptor getVisualModelDescriptor() {
        return new VisualCircuitDescriptor();
    }

}
