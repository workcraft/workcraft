package org.workcraft.plugins.mpsat_synthesis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SynthesisMode {

    RESOLVE_ENCODING_CONFLICTS("-R", Arrays.asList("-$1", "-p0", "-fl")),
    COMPLEX_GATE_IMPLEMENTATION("-Ie", Arrays.asList("-!")),
    GENERALISED_CELEMENT_IMPLEMENTATION("-Ig", Arrays.asList("-!")),
    STANDARD_CELEMENT_IMPLEMENTATION("-Is", Arrays.asList("-!")),
    TECH_MAPPING("-T", Arrays.asList("-p2", "-fl", "-!"));

    private final String mode;
    private final List<String> options;

    SynthesisMode(String mode, List<String> options) {
        this.mode = mode;
        this.options = options;
    }

    public List<String> getMpsatArguments(String modeParameter) {
        List<String> result = new ArrayList<>();
        if (modeParameter == null) {
            result.add(mode);
        } else {
            result.add(mode + "=" + modeParameter);
        }
        result.addAll(options);
        return result;
    }

}
