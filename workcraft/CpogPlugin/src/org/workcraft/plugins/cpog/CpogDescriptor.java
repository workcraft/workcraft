package org.workcraft.plugins.cpog;

import org.workcraft.dom.ModelDescriptor;

public class CpogDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Conditional Partial Order Graph";
    }

    @Override
    public Cpog createMathModel() {
        return new Cpog();
    }

    @Override
    public VisualCpogDescriptor getVisualModelDescriptor() {
        return new VisualCpogDescriptor();
    }

}
