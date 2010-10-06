package org.workcraft.plugins.cpog;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;

public class CpogModelDescriptor implements ModelDescriptor {
	@Override
	public org.workcraft.dom.math.MathModel createMathModel() {
		return new CPOG();
	}

	@Override
	public String getDisplayName() {
		return "Conditional Partial Order Graph";
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new VisualCpogModelDescriptor();
	}
};
