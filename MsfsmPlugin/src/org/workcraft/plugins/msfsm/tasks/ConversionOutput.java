package org.workcraft.plugins.msfsm.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

import java.io.File;

public class ConversionOutput extends ExternalProcessOutput {

    private final File[] files;

    public ConversionOutput(ExternalProcessOutput output, File[] files) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.files = files;
    }

    public File[] getFiles() {
        return files;
    }

}
