package org.workcraft.plugins.policy.tools;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.policy.BundledTransition;

public class BundledTransitionGeneratorTool extends NodeGeneratorTool {

    public BundledTransitionGeneratorTool() {
        super(new DefaultNodeGenerator(BundledTransition.class));
    }

}

