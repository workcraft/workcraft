package org.workcraft.plugins.pcomp.tasks;

import java.io.File;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;

public class PcompOutput extends ExternalProcessOutput {

    private final File[] inputFiles;
    private final File outputFile;
    private final File detailFile;

    public PcompOutput(ExternalProcessOutput output) {
        this(output, null, null, null);
    }

    public PcompOutput(ExternalProcessOutput output, File[] inputFiles, File outptFile, File detailFile) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.inputFiles = inputFiles;
        this.outputFile = outptFile;
        this.detailFile = detailFile;
    }

    public File[] getInputFiles() {
        return inputFiles;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public File getDetailFile() {
        return detailFile;
    }

}
