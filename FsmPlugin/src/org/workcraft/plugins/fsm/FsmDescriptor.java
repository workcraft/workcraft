package org.workcraft.plugins.fsm;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class FsmDescriptor  implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Finite State Machine";
    }

    @Override
    public MathModel createMathModel() {
        return new Fsm();
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualFsmDescriptor();
    }

}
