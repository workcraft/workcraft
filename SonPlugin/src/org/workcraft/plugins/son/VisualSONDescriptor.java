package org.workcraft.plugins.son;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;

public class VisualSONDescriptor implements VisualModelDescriptor {

    public VisualModel create(MathModel mathModel) {
        return new VisualSON((SON) mathModel);
    }

}
