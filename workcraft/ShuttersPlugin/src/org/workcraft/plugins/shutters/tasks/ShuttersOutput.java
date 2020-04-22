package org.workcraft.plugins.shutters.tasks;

public class ShuttersOutput {
    private final String stdout;
    private final String error;

    public ShuttersOutput(String error, String stdout) {
        this.stdout = stdout;
        this.error = error;
    }

    public String getStdout() {
        return this.stdout;
    }

    public String getError() {
        return this.error;
    }
}
