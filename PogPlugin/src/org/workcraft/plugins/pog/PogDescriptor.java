package org.workcraft.plugins.pog;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class PogDescriptor implements ModelDescriptor {
    @Override
    public String getDisplayName() {
        return "Partial Order Graph";
    }

    @Override
    public MathModel createMathModel() {
        return new Pog();
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualPogDescriptor();
    }

}
