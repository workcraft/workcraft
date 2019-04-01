package org.workcraft.plugins.mpsat;

public enum VerificationMode {
    UNDEFINED(null, null, false, false), // Special mode to integrate foreign tasks into MPSat toolchain (export, composition, unfolding)
    DEADLOCK("-D", "Deadlock checking", false, true),
    REACHABILITY("-F", "Reachability analysis", true, true),
    REACHABILITY_REDUNDANCY("-F", "Redundancy analysis", true, true),
    STG_REACHABILITY("-Fs", "STG reachability analysis", true, true),
    STG_REACHABILITY_CONSISTENCY("-Fs", "STG reachability analysis for consistency", true, true),
    STG_REACHABILITY_OUTPUT_PERSISTENCY("-Fs", "STG reachability analysis for output persistency", true, true),
    STG_REACHABILITY_CONFORMATION("-Fs", "STG reachability analysis for conformation", true, true),
    STG_REACHABILITY_CONFORMATION_NWAY("-Fe", "STG reachability analysis for N-way conformation", true, true),
    STG_REACHABILITY_OUTPUT_DETERMINACY("-Fe", "STG output determinacy", true, true),
    CSC_CONFLICT_DETECTION("-C", "CSC conflict detection", false, true),
    NORMALCY("-N", "Normalcy property checking", false, true),
    RESOLVE_ENCODING_CONFLICTS("-R -$1 -p0 -cl", "Resolve encoding conflicts", false, false),
    USC_CONFLICT_DETECTION("-U", "USC conflict detection", false, true),
    COMPLEX_GATE_IMPLEMENTATION("-E", "Derive complex-gate implementation", false, true),
    GENERALISED_CELEMENT_IMPLEMENTATION("-G", "Derive gC-elements implementation", false, true),
    STANDARD_CELEMENT_IMPLEMENTATION("-S", "Derive standard-C implementation", false, true),
    TECH_MAPPING("-T", "Logic decomposition and technology mapping (not finished yet)", false, true),
    ASSERTION("-Fa", "Check asynchronous assertion", true, true);

    private String argument;
    private String description;
    private boolean hasExpression;
    private boolean canPnml;

    VerificationMode(String argument, String description, boolean hasExpression, boolean canPnml) {
        this.argument = argument;
        this.description = description;
        this.hasExpression = hasExpression;
        this.canPnml = canPnml;
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

    public boolean canPnml() {
        return canPnml;
    }

}
