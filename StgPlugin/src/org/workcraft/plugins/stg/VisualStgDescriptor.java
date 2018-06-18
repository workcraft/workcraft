package org.workcraft.plugins.stg;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;

public class VisualStgDescriptor implements VisualModelDescriptor {

    @Override
    public VisualModel create(MathModel mathModel) {
        return new VisualStg((Stg) mathModel);
    }

}
