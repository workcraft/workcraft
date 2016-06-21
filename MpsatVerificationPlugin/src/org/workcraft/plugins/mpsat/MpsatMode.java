/**
 *
 */
package org.workcraft.plugins.mpsat;

public enum MpsatMode {
    UNDEFINED(null, null, false, false), // Special mode to integrate foreign tasks into MPSat toolchain (export, composition, unfolding)
    DEADLOCK("-D", "Deadlock checking", false, true),
    REACHABILITY("-F", "Reachability analysis", true, true),
    STG_REACHABILITY("-Fs", "STG reachability analysis", true, true),
    CSC_CONFLICT_DETECTION("-C", "CSC conflict detection", false, true),
    NORMALCY("-N", "Normalcy property checking", false, true),
    RESOLVE_ENCODING_CONFLICTS("-R -$1 -p0 -cl", "Resolve encoding conflicts", false, false),
    USC_CONFLICT_DETECTION("-U", "USC conflict detection", false, true),
    COMPLEX_GATE_IMPLEMENTATION("-E", "Derive complex-gate implementation", false, true),
    GENERALISED_CELEMENT_IMPLEMENTATION("-G", "Derive gC-elements implementation", false, true),
    STANDARD_CELEMENT_IMPLEMENTATION("-S", "Derive standard-C implementation", false, true),
    TECH_MAPPING("-T", "Logic decomposition and technology mapping (not finished yet)", false, false),
    SVA_INVARIANT("-Sva", "SVA invariant checking", true, true);

    private String argument;
    private String description;
    private boolean hasExpression;
    private boolean canPnml;

    MpsatMode(String argument, String description, boolean hasExpression, boolean canPnml) {
        this.argument = argument;
        this.description = description;
        this.hasExpression = hasExpression;
        this.canPnml = canPnml;
    }

    public static MpsatMode getModeByArgument(String arg) {
        MpsatMode result = null;
        for (MpsatMode mode: MpsatMode.values()) {
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
