package org.workcraft.plugins.dfs;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class DfsDescriptor implements ModelDescriptor {
    @Override
    public String getDisplayName() {
        return "Dataflow Structure";
    }

    @Override
    public MathModel createMathModel() {
        return new Dfs();
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualDfsDescriptor();
    }
}
