package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.tasks.ExportOutput;

import java.io.File;
import java.util.List;

public class MultiExportOutput extends ExportOutput {

    private final List<File> files;

    public MultiExportOutput(List<File> files) {
        super(null);
        this.files = files;
    }

    public List<File> getFiles() {
        return files;
    }

}
