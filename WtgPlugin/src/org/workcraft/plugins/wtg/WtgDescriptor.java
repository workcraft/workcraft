package org.workcraft.plugins.wtg;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class WtgDescriptor  implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Waveform Transition Graph";
    }

    @Override
    public MathModel createMathModel() {
        return new Wtg();
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualWtgDescriptor();
    }

}
