package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.traces.Solution;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MpsatOutput extends ExternalProcessOutput {

    private final VerificationParameters verificationParameters;
    private final File netFile;
    private final File unfoldingFile;
    private final List<Solution> solutions;

    public MpsatOutput(ExternalProcessOutput output, VerificationParameters verificationParameters,
            File netFile, File unfoldingFile, List<Solution> solutions) {

        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.netFile = netFile;
        this.unfoldingFile = unfoldingFile;
        this.solutions = solutions == null ? null : new ArrayList<>(solutions);
        this.verificationParameters = verificationParameters;
    }

    public File getNetFile() {
        return netFile;
    }

    public File getUnfoldingFile() {
        return unfoldingFile;
    }

    public List<Solution> getSolutions() {
        return Collections.unmodifiableList(solutions);
    }

    public VerificationParameters getVerificationParameters() {
        return verificationParameters;
    }

    public boolean hasSolutions() {
        return (solutions != null) && !solutions.isEmpty();
    }

}
