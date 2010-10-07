package org.workcraft.plugins.mpsat;

import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;
import org.workcraft.plugins.shared.presets.Preset;

public class MpsatBuiltinPresets {
	public static final Preset<MpsatSettings> DEADLOCK = new Preset<MpsatSettings>("Deadlock", new MpsatSettings(MpsatMode.DEADLOCK, 0, 0, SolutionMode.FIRST, 0, ""), true);
	public static final Preset<MpsatSettings> DEADLOCK_SHORTEST_TRACE = new Preset<MpsatSettings>("Deadlock (shortest trace)", new MpsatSettings(MpsatMode.DEADLOCK, 0, 0, SolutionMode.MINIMUM_COST, 0, ""), true);
	public static final Preset<MpsatSettings> DEADLOCK_ALL_TRACES = new Preset<MpsatSettings>("Deadlock (all traces)", new MpsatSettings(MpsatMode.DEADLOCK, 0, 0, SolutionMode.ALL, 0, ""), true);
}
