package org.workcraft.plugins.mpsat_temporal.tasks;

import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.traces.Solution;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MpsatOutput extends ExternalProcessOutput {

    private final File netFile;
    private final List<Solution> solutions;

    public MpsatOutput(ExternalProcessOutput output, File netFile, List<Solution> solutions) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.netFile = netFile;
        this.solutions = solutions == null ? null : new ArrayList<>(solutions);
    }

    public File getNetFile() {
        return netFile;
    }

    public List<Solution> getSolutions() {
        return Collections.unmodifiableList(solutions);
    }

    public boolean hasSolutions() {
        return (solutions != null) && !solutions.isEmpty();
    }

}
