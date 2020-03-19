package org.workcraft.plugins.policy;

import org.workcraft.dom.ModelDescriptor;

public class PolicyDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Policy Net";
    }

    @Override
    public Policy createMathModel() {
        return new Policy();
    }

    @Override
    public VisualPolicyDescriptor getVisualModelDescriptor() {
        return new VisualPolicyDescriptor();
    }

    @Override
    public Rating getRating() {
        return Rating.EXPERIMENTAL;
    }

}
