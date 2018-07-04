package org.workcraft.plugins.son;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class SONDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Structured Occurrence Nets";
    }

    @Override
    public MathModel createMathModel() {
        return new SON();
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualSONDescriptor();
    }

}
