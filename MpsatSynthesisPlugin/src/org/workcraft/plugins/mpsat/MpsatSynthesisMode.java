/**
 *
 */
package org.workcraft.plugins.mpsat;

public enum MpsatSynthesisMode {
	COMPLEX_GATE_IMPLEMENTATION ("-E -!", "Derive complex-gate implementation", true, false),
	GENERALISED_CELEMENT_IMPLEMENTATION ("-G -!", "Derive gC-elements implementation", true, false),
	STANDARD_CELEMENT_IMPLEMENTATION ("-S -!", "Derive standard-C implementation", true, false),
	TECH_MAPPING ("-T -f -p2 -cl -!", "Logic decomposition and technology mapping (not finished yet)", true, true);

	private final String argument;
	private final String description;
	private final boolean canPnml;
	private final boolean needLib;

	MpsatSynthesisMode(String argument, String description, boolean canPnml, boolean needLib) {
		this.argument = argument;
		this.description = description;
		this.canPnml = canPnml;
		this.needLib = needLib;
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
		return canPnml;
	}

	public boolean needLib() {
		return needLib;
	}

}