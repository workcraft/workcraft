package org.workcraft.plugins.petrify.tools;

public class PetrifySynthesisTechnologyMapping extends PetrifySynthesis {

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
