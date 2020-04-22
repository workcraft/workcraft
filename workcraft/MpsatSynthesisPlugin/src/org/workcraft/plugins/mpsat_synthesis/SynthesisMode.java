package org.workcraft.plugins.mpsat_synthesis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum SynthesisMode {

    RESOLVE_ENCODING_CONFLICTS(Arrays.asList("-R", "-$1", "-p0", "-cl", "-f"), false),
    COMPLEX_GATE_IMPLEMENTATION(Arrays.asList("-E", "-!"), false),
    GENERALISED_CELEMENT_IMPLEMENTATION(Arrays.asList("-G", "-!"), false),
    STANDARD_CELEMENT_IMPLEMENTATION(Arrays.asList("-S", "-!"), false),
    TECH_MAPPING(Arrays.asList("-T", "-f", "-p2", "-cl", "-!"), true);

    private final List<String> arguments;
    private final boolean needGateLibary;

    SynthesisMode(List<String> arguments, boolean needGateLibary) {
        this.arguments = arguments;
        this.needGateLibary = needGateLibary;
    }

    public List<String> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public boolean needGateLibrary() {
        return needGateLibary;
    }

}
