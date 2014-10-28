package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.MpsatMode;

public class MpsatSynthesisGeneralisedCelement extends MpsatSynthesis {

	public MpsatSynthesisGeneralisedCelement(Framework framework) {
		super(framework);
	}

	@Override
	public MpsatMode getSynthesisMode() {
		return MpsatMode.GENERALISED_CELEMENT_IMPLEMENTATION;
	}

	@Override
	public String getDisplayName() {
		return "Generalised C-element synthesis [MPSat]";
	}

}
