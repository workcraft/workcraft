package org.workcraft.plugins.circuit;

import org.workcraft.Version;
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

    @Override
    public Version getCompatibilityVersion() {
        return new Version(3, 5, 0, Version.Status.RELEASE);
    }

}
