package org.workcraft.plugins.dfs;

import org.workcraft.dom.ModelDescriptor;

public class DfsDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Dataflow Structure";
    }

    @Override
    public Dfs createMathModel() {
        return new Dfs();
    }

    @Override
    public VisualDfsDescriptor getVisualModelDescriptor() {
        return new VisualDfsDescriptor();
    }

}
