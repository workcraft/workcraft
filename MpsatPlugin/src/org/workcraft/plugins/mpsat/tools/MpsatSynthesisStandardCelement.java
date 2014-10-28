package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.MpsatMode;

public class MpsatSynthesisStandardCelement extends MpsatSynthesis {

	public MpsatSynthesisStandardCelement(Framework framework) {
		super(framework);
	}

	@Override
	public MpsatMode getSynthesisMode() {
		return MpsatMode.STANDARD_CELEMENT_IMPLEMENTATION;
	}

	@Override
	public String getDisplayName() {
		return "Standard C-element synthesis [MPSat]";
	}

}
