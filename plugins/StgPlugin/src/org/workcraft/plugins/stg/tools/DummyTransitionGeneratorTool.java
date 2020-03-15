package org.workcraft.plugins.stg.tools;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.stg.DummyTransition;

public class DummyTransitionGeneratorTool extends NodeGeneratorTool {

    public DummyTransitionGeneratorTool() {
        super(new DefaultNodeGenerator(DummyTransition.class));
    }

}

