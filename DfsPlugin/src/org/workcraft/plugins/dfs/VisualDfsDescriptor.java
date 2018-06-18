package org.workcraft.plugins.dfs;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;

public class VisualDfsDescriptor implements VisualModelDescriptor {

    @Override
    public VisualModel create(MathModel mathModel) {
        return new VisualDfs((Dfs) mathModel);
    }

}
