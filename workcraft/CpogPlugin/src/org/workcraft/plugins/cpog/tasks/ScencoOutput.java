package org.workcraft.plugins.cpog.tasks;

public class ScencoOutput {
    private final String stdout;
    private final String error;
    private final String resultDirectoryPath;

    public ScencoOutput(String stdout, String resultDirectoryPath) {
        this.stdout = stdout;
        this.error = null;
        this.resultDirectoryPath = resultDirectoryPath;
    }

    public ScencoOutput(String error, String stdout, String resultDirectoryPath) {
        this.stdout = stdout;
        this.error = error;
        this.resultDirectoryPath = resultDirectoryPath;
    }

    public String getStdout() {
        return this.stdout;
    }

    public String getError() {
        return this.error;
    }

    public String getResultDirectory() {
        return resultDirectoryPath;
    }
}
