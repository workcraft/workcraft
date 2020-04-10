package org.workcraft.plugins.pcomp.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class PcompOutput extends ExternalProcessOutput {

    private final Collection<File> inputFiles;
    private final File outputFile;
    private final File detailFile;

    public PcompOutput(ExternalProcessOutput output, Collection<File> inputFiles, File outputFile, File detailFile) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.inputFiles = inputFiles;
        this.outputFile = outputFile;
        this.detailFile = detailFile;
    }

    public Collection<File> getInputFiles() {
        return Collections.unmodifiableCollection(inputFiles);
    }

    public File getOutputFile() {
        return outputFile;
    }

    public File getDetailFile() {
        return detailFile;
    }

}
