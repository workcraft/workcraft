package org.workcraft.plugins.son;

import org.workcraft.dom.ModelDescriptor;

public class SONDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Structured Occurrence Nets";
    }

    @Override
    public SON createMathModel() {
        return new SON();
    }

    @Override
    public VisualSONDescriptor getVisualModelDescriptor() {
        return new VisualSONDescriptor();
    }

}
