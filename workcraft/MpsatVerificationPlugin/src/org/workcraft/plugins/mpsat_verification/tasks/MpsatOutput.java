package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.traces.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MpsatOutput extends ExternalProcessOutput {

    private final Stg inputStg;
    private final List<Solution> solutions;
    private final VerificationParameters verificationParameters;

    public MpsatOutput(ExternalProcessOutput output, Stg inputStg, List<Solution> solutions,
            VerificationParameters verificationParameters) {

        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.inputStg = inputStg;
        this.solutions = solutions == null ? null : new ArrayList<>(solutions);
        this.verificationParameters = verificationParameters;
    }

    public Stg getInputStg() {
        return inputStg;
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
