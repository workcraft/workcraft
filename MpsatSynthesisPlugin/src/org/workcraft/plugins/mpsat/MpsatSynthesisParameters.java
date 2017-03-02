package org.workcraft.plugins.mpsat;

import java.io.File;
import java.util.ArrayList;

public class MpsatSynthesisParameters {
    private final String name;
    private final MpsatSynthesisMode mode;
    private final int verbosity;

    public MpsatSynthesisParameters(String name, MpsatSynthesisMode mode, int verbosity) {
        this.name = name;
        this.mode = mode;
        this.verbosity = verbosity;
    }

    public String getName() {
        return name;
    }

    public MpsatSynthesisMode getMode() {
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
