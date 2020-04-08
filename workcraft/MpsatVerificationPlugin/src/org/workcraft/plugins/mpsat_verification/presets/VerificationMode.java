package org.workcraft.plugins.mpsat_verification.presets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum VerificationMode {
    UNDEFINED(null), // Special mode to integrate foreign tasks into MPSat toolchain (export, composition, unfolding)
    DEADLOCK(Arrays.asList("-D")),
    REACHABILITY(Arrays.asList("-F")),
    REACHABILITY_REDUNDANCY(Arrays.asList("-F")),
    STG_REACHABILITY(Arrays.asList("-Fs")),
    STG_REACHABILITY_CONSISTENCY(Arrays.asList("-Fs")),
    STG_REACHABILITY_OUTPUT_PERSISTENCY(Arrays.asList("-Fs")),
    STG_REACHABILITY_CONFORMATION(Arrays.asList("-Fs")),
    STG_REACHABILITY_CONFORMATION_NWAY(Arrays.asList("-Fe")),
    STG_REACHABILITY_OUTPUT_DETERMINACY(Arrays.asList("-Fe")),
    CSC_CONFLICT_DETECTION(Arrays.asList("-C")),
    NORMALCY(Arrays.asList("-N")),
    USC_CONFLICT_DETECTION(Arrays.asList("-U")),
    ASSERTION(Arrays.asList("-Fa"));

    private List<String> arguments;

    VerificationMode(List<String> arguments) {
        this.arguments = arguments;
    }

    public List<String> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

}
