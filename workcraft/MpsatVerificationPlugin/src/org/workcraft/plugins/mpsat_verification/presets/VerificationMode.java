package org.workcraft.plugins.mpsat_verification.presets;

public enum VerificationMode {
    UNDEFINED(null), // Special mode to integrate foreign tasks into MPSat toolchain (export, composition, unfolding)
    DEADLOCK("-D"),
    ASSERTION("-Fa"),
    REACHABILITY("-F"),
    REACHABILITY_REDUNDANCY("-F"),
    STG_REACHABILITY("-Fs"),
    STG_REACHABILITY_CONSISTENCY("-Fs"),
    STG_REACHABILITY_OUTPUT_PERSISTENCY("-Fs"),
    STG_REACHABILITY_REFINEMENT("-Fe"),
    STG_REACHABILITY_CONFORMATION("-Fe"),
    STG_REACHABILITY_OUTPUT_DETERMINACY("-Fe"),
    NORMALCY("-Cn"),
    CSC_CONFLICT_DETECTION("-Cc"),
    USC_CONFLICT_DETECTION("-Cu");

    private String modeArg;

    VerificationMode(String modeArg) {
        this.modeArg = modeArg;
    }

    public String getModeArgument() {
        return modeArg;
    }

}
