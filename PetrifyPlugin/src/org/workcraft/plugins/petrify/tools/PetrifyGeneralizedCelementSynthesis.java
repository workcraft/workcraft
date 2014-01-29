package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;

public class PetrifyGeneralizedCelementSynthesis extends PetrifySynthesis {

	public PetrifyGeneralizedCelementSynthesis(Framework framework) {
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
		return "Generalized C-element synthesis (Petrify)";
	}
}
