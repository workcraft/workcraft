package org.workcraft.plugins.wtg;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;

public class VisualWtgDescriptor implements VisualModelDescriptor {

    @Override
    public VisualModel create(MathModel mathModel) {
        return new VisualWtg((Wtg) mathModel);
    }

}
