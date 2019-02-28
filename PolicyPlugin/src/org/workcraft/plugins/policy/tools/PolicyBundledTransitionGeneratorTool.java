package org.workcraft.plugins.policy.tools;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.policy.BundledTransition;

public class PolicyBundledTransitionGeneratorTool extends NodeGeneratorTool {

    public PolicyBundledTransitionGeneratorTool() {
        super(new DefaultNodeGenerator(BundledTransition.class));
    }

}

