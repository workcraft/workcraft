package org.workcraft.plugins.punf.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

public class PunfOutput extends ExternalProcessOutput {

    public PunfOutput(ExternalProcessOutput output) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
    }

}
