package org.workcraft.plugins.xbm;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class XbmDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "eXtended Burst-mode Machine";
    }

    @Override
    public MathModel createMathModel() {
        return new Xbm();
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualXbmDescriptor();
    }
}
