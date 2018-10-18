package org.workcraft.plugins.wtg;

import org.workcraft.dom.ModelDescriptor;

public class WtgDescriptor  implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Waveform Transition Graph";
    }

    @Override
    public Wtg createMathModel() {
        return new Wtg();
    }

    @Override
    public VisualWtgDescriptor getVisualModelDescriptor() {
        return new VisualWtgDescriptor();
    }

    @Override
    public Rating getRating() {
        return Rating.EXPERIMENTAL;
    }

}
