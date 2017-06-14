package org.workcraft.plugins.fst.task;

public class LtscatResult {
    private final String stdout;
    private final String error;

    public LtscatResult(String stdout) {
        this.stdout = stdout;
        this.error = null;
    }

    public LtscatResult(String error, String stdout) {
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
