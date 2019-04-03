package org.workcraft.plugins.petri.tools;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.petri.Transition;

public class TransitionGeneratorTool extends NodeGeneratorTool {

    public TransitionGeneratorTool() {
        super(new DefaultNodeGenerator(Transition.class));
    }

}

