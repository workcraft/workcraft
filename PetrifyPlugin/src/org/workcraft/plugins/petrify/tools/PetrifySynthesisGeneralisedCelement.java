package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;

public class PetrifySynthesisGeneralisedCelement extends PetrifySynthesis {

	public PetrifySynthesisGeneralisedCelement(Framework framework) {
		super(framework);
	}

	@Override
	public String[] getSynthesisParameter() {
		String[] result = new String[1];
		result[0] = "-gc";
		return result;
	}

	@Override
	public String getDisplayName() {
		return "Generalized C-element synthesis [Petrify]";
	}
}
