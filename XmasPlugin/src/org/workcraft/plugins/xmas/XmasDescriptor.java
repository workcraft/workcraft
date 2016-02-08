package org.workcraft.plugins.xmas;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class XmasDescriptor implements ModelDescriptor {

    @Override
    public MathModel createMathModel() {
        return new Xmas();
    }

    @Override
    public String getDisplayName() {
        return "xMAS Circuit";
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualXmasDescriptor();
    }

}
