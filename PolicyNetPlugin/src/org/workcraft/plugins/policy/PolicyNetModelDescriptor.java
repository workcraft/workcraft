package org.workcraft.plugins.policy;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class PolicyNetModelDescriptor implements ModelDescriptor
{
	@Override
	public String getDisplayName() {
		return "Policy Net";
	}

	@Override
	public MathModel createMathModel() {
		return new PolicyNet();
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new PolicyNetVisualModelDescriptor();
	}
}
