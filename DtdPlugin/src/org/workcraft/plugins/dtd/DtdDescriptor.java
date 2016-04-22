package org.workcraft.plugins.dtd;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class DtdDescriptor  implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Digital Timing Diagram";
    }

    @Override
    public MathModel createMathModel() {
        return new Dtd();
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualDtdDescriptor();
    }

}
