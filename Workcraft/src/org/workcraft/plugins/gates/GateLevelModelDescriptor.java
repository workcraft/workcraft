package org.workcraft.plugins.gates;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class GateLevelModelDescriptor implements ModelDescriptor {

	@Override
	public String getDisplayName() {
		return "Gate-level circuit";
	}

	@Override
	public MathModel createMathModel() {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

}
