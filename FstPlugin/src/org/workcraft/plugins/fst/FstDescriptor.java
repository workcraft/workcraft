package org.workcraft.plugins.fst;

import org.workcraft.dom.ModelDescriptor;

public class FstDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Finite State Transducer";
    }

    @Override
    public Fst createMathModel() {
        return new Fst();
    }

    @Override
    public VisualFstDescriptor getVisualModelDescriptor() {
        return new VisualFstDescriptor();
    }

}
