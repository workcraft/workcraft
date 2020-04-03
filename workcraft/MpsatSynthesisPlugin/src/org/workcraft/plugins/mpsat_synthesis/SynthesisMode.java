package org.workcraft.plugins.mpsat_synthesis;

public enum SynthesisMode {

    RESOLVE_ENCODING_CONFLICTS("-R -$1 -p0 -cl -f", false),
    COMPLEX_GATE_IMPLEMENTATION("-E -!", false),
    GENERALISED_CELEMENT_IMPLEMENTATION("-G -!", false),
    STANDARD_CELEMENT_IMPLEMENTATION("-S -!", false),
    TECH_MAPPING("-T -f -p2 -cl -!", true);

    private final String argument;
    private final boolean needLib;

    SynthesisMode(String argument, boolean needLib) {
        this.argument = argument;
        this.needLib = needLib;
    }

    public String getArgument() {
        return argument;
    }

    public boolean needLib() {
        return needLib;
    }

}
