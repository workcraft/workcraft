package org.workcraft.plugins.cpog;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class CpogDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Conditional Partial Order Graph";
    }

    @Override
    public MathModel createMathModel() {
        return new Cpog();
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualCpogDescriptor();
    }

}
