package org.workcraft.plugins.petrify.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

public class WriteSgConversionOutput extends ExternalProcessOutput {

    public WriteSgConversionOutput(ExternalProcessOutput output) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
    }

    public byte[] getFstBytes() {
        return getStdout();
    }

}
