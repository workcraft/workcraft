package org.workcraft.plugins.petrify.tools;

public class PetrifySynthesisComplexGate extends PetrifySynthesis {

	@Override
	public String[] getSynthesisParameter() {
		String[] result = new String[1];
		result[0] = "-cg";
		return result;
	}

	@Override
	public String getDisplayName() {
		return "Complex gate synthesis [Petrify]";
	}
}
