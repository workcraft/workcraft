package org.workcraft.plugins.petrify.tasks;

import java.io.File;

public class DrawSgResult {
    private final File file;
    private final String errorMessages;

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
