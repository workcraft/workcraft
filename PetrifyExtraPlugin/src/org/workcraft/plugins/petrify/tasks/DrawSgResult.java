package org.workcraft.plugins.petrify.tasks;

import java.io.File;

public class DrawSgResult {
    private File file = null;
    private String errorMessages = null;

    public DrawSgResult(File file, String errorMessages) {
        this.file = file;
        this.errorMessages = errorMessages;
    }

    public File getFile() {
        return file;
    }

    public String getErrorMessages() {
        return errorMessages;
    }
}
