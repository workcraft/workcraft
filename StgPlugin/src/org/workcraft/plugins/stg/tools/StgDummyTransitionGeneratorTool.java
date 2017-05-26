package org.workcraft.plugins.stg.tools;

import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.stg.DummyTransition;

public class StgDummyTransitionGeneratorTool extends NodeGeneratorTool {

    public StgDummyTransitionGeneratorTool() {
        super(new DefaultNodeGenerator(DummyTransition.class));
    }

}

