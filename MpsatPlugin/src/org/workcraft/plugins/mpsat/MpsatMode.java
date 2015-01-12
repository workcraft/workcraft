/**
 *
 */
package org.workcraft.plugins.mpsat;

public enum MpsatMode {
	UNDEFINED (null, null, false), // Special mode to integrate foreign tasks into Mpsat toolchain (export, composition, unfolding)
	DEADLOCK ("-D", "Deadlock checking", false),
	REACHABILITY ("-F", "Reachability analysis", true),
	STG_REACHABILITY ("-Fs", "STG reachability analysis", true),
	CSC_CONFLICT_DETECTION ("-C", "CSC conflict detection", false),
	NORMALCY ("-N", "Normalcy property checking", false),
	RESOLVE_ENCODING_CONFLICTS ("-R -$1 -p0 -cl", "Resolve encoding conflicts", false),
	USC_CONFLICT_DETECTION ("-U", "USC conflict detection", false),
	COMPLEX_GATE_IMPLEMENTATION ("-E", "Derive complex-gate implementation", false),
	GENERALISED_CELEMENT_IMPLEMENTATION ("-G", "Derive gC-elements implementation", false),
	STANDARD_CELEMENT_IMPLEMENTATION ("-S", "Derive standard-C implementation", false),
	TECH_MAPPING ("-T", "Logic decomposition and technology mapping (not finished yet)", false);

	private String argument;
	private String description;
	private boolean reach;

	MpsatMode(String argument, String description, boolean reach) {
		this.argument = argument;
		this.description = description;
		this.reach = reach;
	}

	public static MpsatMode getModeByArgument(String arg) {
		MpsatMode result = null;
		for (MpsatMode mode: MpsatMode.values()) {
			if (mode.getArgument().equals(arg)) {
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

	public boolean isReach() {
		return reach;
	}
}