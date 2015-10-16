package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatSynthesisMode;

public class MpsatSynthesisComplexGate extends MpsatSynthesis {

	@Override
	public MpsatSynthesisMode getSynthesisMode() {
		return MpsatSynthesisMode.COMPLEX_GATE_IMPLEMENTATION;
	}

	@Override
	public String getDisplayName() {
		return "Complex gate [MPSat]";
	}

}
