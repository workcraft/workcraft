package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.MpsatMode;

public class MpsatSynthesisTechnologyMapping extends MpsatSynthesis {

	public MpsatSynthesisTechnologyMapping(Framework framework) {
		super(framework);
	}

	@Override
	public MpsatMode getSynthesisMode() {
		return MpsatMode.TECH_MAPPING;
	}

	@Override
	public String getDisplayName() {
		return "Technology mapping [MPSat]";
	}

}
