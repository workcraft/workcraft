package org.workcraft.plugins.stg;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class StgDescriptor implements ModelDescriptor {
    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public MathModel createMathModel() {
        return new Stg();
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualStgDescriptor();
    }
}
