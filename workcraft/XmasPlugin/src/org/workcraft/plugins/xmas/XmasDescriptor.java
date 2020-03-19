package org.workcraft.plugins.xmas;

import org.workcraft.dom.ModelDescriptor;

public class XmasDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "xMAS Circuit";
    }

    @Override
    public Xmas createMathModel() {
        return new Xmas();
    }

    @Override
    public VisualXmasDescriptor getVisualModelDescriptor() {
        return new VisualXmasDescriptor();
    }

}
