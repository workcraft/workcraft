package org.workcraft.plugins.petrify.tools;

public class PetrifySynthesisGeneralisedCelement extends PetrifySynthesis {

	@Override
	public String[] getSynthesisParameter() {
		String[] result = new String[1];
		result[0] = "-gc";
		return result;
	}

	@Override
	public String getDisplayName() {
		return "Generalized C-element [Petrify]";
	}

	@Override
	public Position getPosition() {
		return Position.TOP;
	}

}
