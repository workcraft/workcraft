package org.workcraft.plugins.mpsat;

import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;
import org.workcraft.plugins.shared.presets.Preset;

public class MpsatBuiltinPresets {

    public static final Preset<MpsatSettings> DEADLOCK_CHECKER_ALL_TRACES = new Preset<>(
            " Deadlock (all traces)", new MpsatSettings("Deadlock freeness",
                    MpsatMode.DEADLOCK, 0, SolutionMode.ALL, 0), true);

    public static final Preset<MpsatSettings> DEADLOCK_CHECKER_SHORTEST_TRACE = new Preset<>(
            " Deadlock (shortest trace)", new MpsatSettings("Deadlock freeness",
                    MpsatMode.DEADLOCK, 0, SolutionMode.MINIMUM_COST, 0), true);

    public static final Preset<MpsatSettings> CONSISTENCY_CHECKER = new Preset<>(
            "Consistency", new MpsatSettings("Consistency",
                    MpsatMode.STG_REACHABILITY, 0, SolutionMode.MINIMUM_COST, 0,
                    MpsatSettings.REACH_CONSISTENCY, true), true);

    public static final Preset<MpsatSettings> INPUT_PROPERNESS_CHECKER = new Preset<>(
            "Input properness", new MpsatSettings("Input properness",
                    MpsatMode.STG_REACHABILITY, 0, SolutionMode.MINIMUM_COST, 0,
                    MpsatSettings.REACH_INPUT_PROPERNESS, true), true);

    public static final Preset<MpsatSettings> DI_INTERFACE_CHECKER = new Preset<>(
            "Delay insensitive interface", new MpsatSettings("Delay insensitive interface",
                    MpsatMode.STG_REACHABILITY, 0, SolutionMode.MINIMUM_COST, 0,
                    MpsatSettings.REACH_DI_INTERFACE, true), true);

    public static final Preset<MpsatSettings> NORMALCY_CHECKER = new Preset<>(
            "Normalcy", new MpsatSettings("Normalcy",
                    MpsatMode.NORMALCY, 0, SolutionMode.MINIMUM_COST, 0), true);

    public static final Preset<MpsatSettings> OUTPUT_PERSISTENCY_CHECKER = new Preset<>(
            "Output persistency (without dummies)", new MpsatSettings("Output persistency",
                    MpsatMode.STG_REACHABILITY, 0, SolutionMode.MINIMUM_COST, 0,
                    MpsatSettings.REACH_OUTPUT_PERSISTENCY, true), true);

}
