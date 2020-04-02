package org.workcraft.plugins.mpsat_synthesis;

import java.io.File;
import java.util.ArrayList;

public class SynthesisParameters {
    private final String name;
    private final SynthesisMode mode;
    private final int verbosity;

    public SynthesisParameters(String name, SynthesisMode mode, int verbosity) {
        this.name = name;
        this.mode = mode;
        this.verbosity = verbosity;
    }

    public String getName() {
        return name;
    }

    public SynthesisMode getMode() {
        return mode;
    }

    public int getVerbosity() {
        return verbosity;
    }

    public String[] getMpsatArguments(File workingDirectory) {
        ArrayList<String> args = new ArrayList<>();
        for (String option: getMode().getArgument().split("\\s")) {
            args.add(option);
        }
        args.add(String.format("-v%d", getVerbosity()));
        return args.toArray(new String[args.size()]);
    }

}
