package org.workcraft.tasks;

import java.io.File;

public class ExportOutput {

    private final File file;

    public ExportOutput(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

}
