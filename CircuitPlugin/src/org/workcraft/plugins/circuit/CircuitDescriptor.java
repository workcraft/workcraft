package org.workcraft.plugins.circuit;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class CircuitDescriptor implements ModelDescriptor {

    @Override
    public MathModel createMathModel() {
        return new Circuit();
    }

    @Override
    public String getDisplayName() {
        return "Digital Circuit";
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualCircuitDescriptor();
    }

}
