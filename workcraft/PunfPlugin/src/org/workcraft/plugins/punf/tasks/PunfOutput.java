package org.workcraft.plugins.punf.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

import java.io.File;

public class PunfOutput extends ExternalProcessOutput {

    private final File outputFile;

    public PunfOutput(ExternalProcessOutput output, File outputFile) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.outputFile = outputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

}
