package org.workcraft.plugins.circuit;

import org.workcraft.Plugin;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class CircuitModelDescriptor implements Plugin, ModelDescriptor {

	@Override
	public MathModel createMathModel() {
		return new Circuit();
	}

	@Override
	public String getDisplayName() {
		return "Digital Circuit";
	}

}
