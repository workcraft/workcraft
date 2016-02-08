package org.workcraft.plugins.cpog.tasks;

public class ScencoResult {
    private final String stdout;
    private final String resultDirectoryPath;

    public ScencoResult(String stdout, String resultDirectoryPath) {
        this.stdout = stdout;
        this.resultDirectoryPath = resultDirectoryPath;
    }

    public String getStdout() {
        return this.stdout;
    }

    public String getResultDirectory() {
        return resultDirectoryPath;
    }
}
