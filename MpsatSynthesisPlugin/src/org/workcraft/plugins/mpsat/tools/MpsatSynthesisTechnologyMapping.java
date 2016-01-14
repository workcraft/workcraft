package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatSynthesisMode;

public class MpsatSynthesisTechnologyMapping extends MpsatSynthesis {

	@Override
	public MpsatSynthesisMode getSynthesisMode() {
		return MpsatSynthesisMode.TECH_MAPPING;
	}

	@Override
	public String getDisplayName() {
		return "Technology mapping [MPSat]";
	}

	@Override
	public Position getPosition() {
		return Position.BOTTOM;
	}

}
