package org.workcraft.plugins.mpsat;

import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;
import org.workcraft.plugins.shared.presets.Preset;

public class MpsatBuiltinPresets {

    public static final Preset<MpsatSettings> DEADLOCK_CHECKER_ALL_TRACES = new Preset<>(
            " Deadlock (all traces)", new MpsatSettings("Deadlock freeness",
                    MpsatMode.DEADLOCK, 0, SolutionMode.ALL, 0), true);

    public static final Preset<MpsatSettings> DEADLOCK_CHECKER = new Preset<>(
            " Deadlock (shortest trace)", new MpsatSettings("Deadlock freeness",
                    MpsatMode.DEADLOCK, 0, SolutionMode.MINIMUM_COST, 0), true);

    public static final Preset<MpsatSettings> CONSISTENCY_CHECKER = new Preset<>(
            "Consistency", new MpsatSettings("Consistency",
                    MpsatMode.STG_REACHABILITY, 0, SolutionMode.MINIMUM_COST, 0,
                    MpsatSettings.reachConsistency, true), true);

    public static final Preset<MpsatSettings> NORMALCY_CHECKER = new Preset<>(
            "Normalcy", new MpsatSettings("Normalcy",
                    MpsatMode.NORMALCY, 0, SolutionMode.MINIMUM_COST, 0), true);

    public static final Preset<MpsatSettings> PERSISTENCY_CHECKER = new Preset<>(
            "Output persistency (without dummies)", new MpsatSettings("Output persistency",
                    MpsatMode.STG_REACHABILITY, 0, SolutionMode.MINIMUM_COST, 0,
                    MpsatSettings.reachSemimodularity, true), true);

}
