package org.workcraft.plugins.mpsat_verification;

public enum VerificationMode {
    UNDEFINED(null), // Special mode to integrate foreign tasks into MPSat toolchain (export, composition, unfolding)
    DEADLOCK("-D"),
    REACHABILITY("-F"),
    REACHABILITY_REDUNDANCY("-F"),
    STG_REACHABILITY("-Fs"),
    STG_REACHABILITY_CONSISTENCY("-Fs"),
    STG_REACHABILITY_OUTPUT_PERSISTENCY("-Fs"),
    STG_REACHABILITY_CONFORMATION("-Fs"),
    STG_REACHABILITY_CONFORMATION_NWAY("-Fe"),
    STG_REACHABILITY_OUTPUT_DETERMINACY("-Fe"),
    CSC_CONFLICT_DETECTION("-C"),
    NORMALCY("-N"),
    USC_CONFLICT_DETECTION("-U"),
    ASSERTION("-Fa");

    private String argument;

    VerificationMode(String argument) {
        this.argument = argument;
    }

    public String getArgument() {
        return argument;
    }

}
