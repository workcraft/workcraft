package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.MpsatMode;

public class MpsatSynthesisComplexGate extends MpsatSynthesis {

	public MpsatSynthesisComplexGate(Framework framework) {
		super(framework);
	}

	@Override
	public MpsatMode getSynthesisMode() {
		return MpsatMode.COMPLEX_GATE_IMPLEMENTATION;
	}

	@Override
	public String getDisplayName() {
		return "Complex gate synthesis [MPSat]";
	}

}
