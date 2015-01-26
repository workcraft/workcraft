package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatMode;

public class MpsatSynthesisStandardCelement extends MpsatSynthesis {

	@Override
	public MpsatMode getSynthesisMode() {
		return MpsatMode.STANDARD_CELEMENT_IMPLEMENTATION;
	}

	@Override
	public String getDisplayName() {
		return "Standard C-element [MPSat]";
	}

}
