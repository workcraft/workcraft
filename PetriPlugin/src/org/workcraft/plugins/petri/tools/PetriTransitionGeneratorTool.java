package org.workcraft.plugins.petri.tools;

import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.petri.Transition;

public class PetriTransitionGeneratorTool extends NodeGeneratorTool {

    public PetriTransitionGeneratorTool() {
        super(new DefaultNodeGenerator(Transition.class));
    }

}

