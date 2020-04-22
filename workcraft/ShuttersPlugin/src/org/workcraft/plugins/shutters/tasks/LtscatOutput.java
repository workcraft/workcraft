package org.workcraft.plugins.shutters.tasks;

public class LtscatOutput {

    private final String stdout;
    private final String error;

    public LtscatOutput(String error, String stdout) {
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
