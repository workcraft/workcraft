/**
 *
 */
package org.workcraft.plugins.shared;

public enum MpsatMode {
	DEADLOCK ("-D", "Deadlock checking", false),
	REACHABILITY ("-F", "Reachability analysis", true),
	STG_REACHABILITY ("-Fs", "STG reachability analysis", true),
	CSC_CONFLICT_DETECTION ("-C", "CSC conflict detection", false),
	NORMALCY ("-N", "Normalcy property checking", false),
	RESOLVE_ENCODING_CONFLICTS ("-R", "Resolve encoding conflicts", false),
	USC_CONFLICT_DETECTION ("-U", "USC conflict detection", false),
	COMPLEX_GATE_IMPLEMENTATION ("-E", "Derive complex-gate implementation", false),
	GC_IMPLEMENTATION ("-G", "Derive gC-elements implementation", false),
	STANDART_C_IMPLEMENTATION ("-S", "Derive standard-C implementation", false),
	TECH_MAPPING ("-T", "Logic decomposition and technology mapping (not finished yet)", false);

	private String argument;
	private String description;
	private boolean reach;

	public static final MpsatMode[] modes =
	{
		DEADLOCK,
		REACHABILITY,
		STG_REACHABILITY,
		CSC_CONFLICT_DETECTION,
		NORMALCY,
		RESOLVE_ENCODING_CONFLICTS,
		USC_CONFLICT_DETECTION,
		COMPLEX_GATE_IMPLEMENTATION,
		GC_IMPLEMENTATION,
		STANDART_C_IMPLEMENTATION,
		TECH_MAPPING
	};

	public static MpsatMode getMode (String arg) {
		for (int i=0; i<modes.length; i++)
			if (modes[i].getArgument().equals(arg))
				return modes[i];
		return null;
	}

	MpsatMode(String argument, String description, boolean reach) {
		this.argument = argument;
		this.description = description;
		this.reach = reach;
	}

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