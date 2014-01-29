package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;

public class PetrifyComplexGateSynthesis extends PetrifySynthesis {

	public PetrifyComplexGateSynthesis(Framework framework) {
		super(framework);
	}

	@Override
	public String[] getSynthesisParameter() {
		String[] result = new String[1];
		result[0] = "-cg";
		return result;
	}

	@Override
	public String getDisplayName() {
		return "Complex gate synthesis (Petrify)";
	}
}
