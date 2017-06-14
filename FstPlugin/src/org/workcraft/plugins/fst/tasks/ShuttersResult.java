package org.workcraft.plugins.fst.tasks;

public class ShuttersResult {
    private final String stdout;
    private final String error;

    public ShuttersResult(String stdout) {
        this.stdout = stdout;
        this.error = null;
    }

    public ShuttersResult(String error, String stdout) {
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
