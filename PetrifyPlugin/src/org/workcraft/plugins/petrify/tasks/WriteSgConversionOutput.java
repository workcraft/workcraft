package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;

public class WriteSgConversionOutput extends ExternalProcessOutput {

    private final Fst fst;

    public WriteSgConversionOutput(ExternalProcessOutput output, Fst fst) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.fst = fst;
    }

    public Fst getFst() {
        return fst;
    }

}
