package org.workcraft.plugins.pcomp.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;

public class PcompOutput extends ExternalProcessOutput {

    public PcompOutput(ExternalProcessOutput output) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
    }

}
