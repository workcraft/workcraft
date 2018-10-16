package org.workcraft.plugins.policy;

import org.workcraft.dom.ModelDescriptor;

public class PolicyNetDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Policy Net";
    }

    @Override
    public PolicyNet createMathModel() {
        return new PolicyNet();
    }

    @Override
    public VisualPolicyNetDescriptor getVisualModelDescriptor() {
        return new VisualPolicyNetDescriptor();
    }

    @Override
    public Rating getRating() {
        return Rating.EXPERIMENTAL;
    }

}
