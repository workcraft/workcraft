package org.workcraft.plugins.stg;

import org.workcraft.dom.ModelDescriptor;

public class StgDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public Stg createMathModel() {
        return new Stg();
    }

    @Override
    public VisualStgDescriptor getVisualModelDescriptor() {
        return new VisualStgDescriptor();
    }

}
