package org.workcraft.plugins.fsm;

import org.workcraft.dom.ModelDescriptor;

public class FsmDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Finite State Machine";
    }

    @Override
    public Fsm createMathModel() {
        return new Fsm();
    }

    @Override
    public VisualFsmDescriptor getVisualModelDescriptor() {
        return new VisualFsmDescriptor();
    }

}
