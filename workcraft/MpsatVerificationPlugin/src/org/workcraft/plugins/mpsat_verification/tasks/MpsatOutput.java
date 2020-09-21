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
    private final File stgFile;
    private final List<Solution> solutions;

    public MpsatOutput(ExternalProcessOutput output, VerificationParameters verificationParameters) {
        this(output, verificationParameters, null, null);
    }

    public MpsatOutput(ExternalProcessOutput output, VerificationParameters verificationParameters,
            File stgFile, List<Solution> solutions) {

        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.stgFile = stgFile;
        this.solutions = solutions == null ? null : new ArrayList<>(solutions);
        this.verificationParameters = verificationParameters;
    }

    public File getStgFile() {
        return stgFile;
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
