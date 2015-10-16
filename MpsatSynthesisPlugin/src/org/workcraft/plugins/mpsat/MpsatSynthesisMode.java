/**
 *
 */
package org.workcraft.plugins.mpsat;

public enum MpsatSynthesisMode {
	COMPLEX_GATE_IMPLEMENTATION ("-E", "Derive complex-gate implementation", true),
	GENERALISED_CELEMENT_IMPLEMENTATION ("-G", "Derive gC-elements implementation", true),
	STANDARD_CELEMENT_IMPLEMENTATION ("-S", "Derive standard-C implementation", true),
	TECH_MAPPING ("-T", "Logic decomposition and technology mapping (not finished yet)", false);

	private String argument;
	private String description;
	private boolean pnml;

	MpsatSynthesisMode(String argument, String description, boolean pnml) {
		this.argument = argument;
		this.description = description;
		this.pnml = pnml;
	}

	public static MpsatSynthesisMode getModeByArgument(String arg) {
		MpsatSynthesisMode result = null;
		for (MpsatSynthesisMode mode: MpsatSynthesisMode.values()) {
			String modeArg = mode.getArgument();
			if ((modeArg != null) && modeArg.equals(arg)) {
				result = mode;
				break;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return description;
	}

	public String getArgument() {
		return argument;
	}

	public boolean canPnml() {
		return pnml;
	}

}