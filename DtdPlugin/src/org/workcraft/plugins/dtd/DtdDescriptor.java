package org.workcraft.plugins.dtd;

import org.workcraft.dom.ModelDescriptor;

public class DtdDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Digital Timing Diagram";
    }

    @Override
    public Dtd createMathModel() {
        return new Dtd();
    }

    @Override
    public VisualDtdDescriptor getVisualModelDescriptor() {
        return new VisualDtdDescriptor();
    }

}
