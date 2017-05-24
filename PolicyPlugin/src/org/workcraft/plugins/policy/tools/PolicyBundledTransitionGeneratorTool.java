package org.workcraft.plugins.policy.tools;

import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.policy.BundledTransition;

public class PolicyBundledTransitionGeneratorTool extends NodeGeneratorTool {

    public PolicyBundledTransitionGeneratorTool() {
        super(new DefaultNodeGenerator(BundledTransition.class));
    }

}

