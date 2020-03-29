package org.workcraft.plugins.mpsat;

public enum VerificationMode {
    UNDEFINED(null, null, false), // Special mode to integrate foreign tasks into MPSat toolchain (export, composition, unfolding)
    DEADLOCK("-D", "Deadlock checking", false),
    REACHABILITY("-F", "Reachability analysis", true),
    REACHABILITY_REDUNDANCY("-F", "Redundancy analysis", true),
    STG_REACHABILITY("-Fs", "STG reachability analysis", true),
    STG_REACHABILITY_CONSISTENCY("-Fs", "STG reachability analysis for consistency", true),
    STG_REACHABILITY_OUTPUT_PERSISTENCY("-Fs", "STG reachability analysis for output persistency", true),
    STG_REACHABILITY_CONFORMATION("-Fs", "STG reachability analysis for conformation", true),
    STG_REACHABILITY_CONFORMATION_NWAY("-Fe", "STG reachability analysis for N-way conformation", true),
    STG_REACHABILITY_OUTPUT_DETERMINACY("-Fe", "STG output determinacy", true),
    CSC_CONFLICT_DETECTION("-C", "CSC conflict detection", false),
    NORMALCY("-N", "Normalcy property checking", false),
    RESOLVE_ENCODING_CONFLICTS("-R -$1 -p0 -cl", "Resolve encoding conflicts", false),
    USC_CONFLICT_DETECTION("-U", "USC conflict detection", false),
    COMPLEX_GATE_IMPLEMENTATION("-E", "Derive complex-gate implementation", false),
    GENERALISED_CELEMENT_IMPLEMENTATION("-G", "Derive gC-elements implementation", false),
    STANDARD_CELEMENT_IMPLEMENTATION("-S", "Derive standard-C implementation", false),
    TECH_MAPPING("-T", "Logic decomposition and technology mapping (not finished yet)", false),
    ASSERTION("-Fa", "Check asynchronous assertion", true);

    private String argument;
    private String description;
    private boolean hasExpression;

    VerificationMode(String argument, String description, boolean hasExpression) {
        this.argument = argument;
        this.description = description;
        this.hasExpression = hasExpression;
    }

    public static VerificationMode getModeByArgument(String arg) {
        VerificationMode result = null;
        for (VerificationMode mode: VerificationMode.values()) {
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

    public boolean hasExpression() {
        return hasExpression;
    }

}
