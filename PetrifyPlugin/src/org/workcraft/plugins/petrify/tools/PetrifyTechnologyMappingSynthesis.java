package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;

public class PetrifyTechnologyMappingSynthesis extends PetrifySynthesis {

	public PetrifyTechnologyMappingSynthesis(Framework framework) {
		super(framework);
	}

	@Override
	public String[] getSynthesisParameter() {
		String[] result = new String[1];
		result[0] = "-tm";
		return result;
	}

	@Override
	public String getDisplayName() {
		return "Technology mapping synthesis [Petrify]";
	}

}
