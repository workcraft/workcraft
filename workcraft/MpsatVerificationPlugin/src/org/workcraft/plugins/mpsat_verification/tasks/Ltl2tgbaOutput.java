package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

import java.io.File;

public class Ltl2tgbaOutput extends ExternalProcessOutput {

    private final File spotFile;
    private final File hoaFile;

    public Ltl2tgbaOutput(ExternalProcessOutput output, File spotFile, File hoaFile) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.spotFile = spotFile;
        this.hoaFile = hoaFile;
    }

    public File getSpotFile() {
        return spotFile;
    }

    public File getHoaFile() {
        return hoaFile;
    }

}
