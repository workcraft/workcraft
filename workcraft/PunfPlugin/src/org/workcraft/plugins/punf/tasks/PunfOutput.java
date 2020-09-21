package org.workcraft.plugins.punf.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

import java.io.File;

public class PunfOutput extends ExternalProcessOutput {

    private final File inputFile;
    private final File outputFile;

    public PunfOutput(ExternalProcessOutput output, File inputFile, File outputFile) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

}
