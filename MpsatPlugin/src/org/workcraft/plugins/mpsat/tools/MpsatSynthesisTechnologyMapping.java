package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatMode;

public class MpsatSynthesisTechnologyMapping extends MpsatSynthesis {

	@Override
	public MpsatMode getSynthesisMode() {
		return MpsatMode.TECH_MAPPING;
	}

	@Override
	public String getDisplayName() {
		return "Technology mapping synthesis [MPSat]";
	}

}
