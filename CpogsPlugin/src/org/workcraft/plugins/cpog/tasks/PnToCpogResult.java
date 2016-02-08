package org.workcraft.plugins.cpog.tasks;

import org.workcraft.plugins.cpog.VisualCPOG;

public class PnToCpogResult {
    private final String stdout;
    private VisualCPOG cpog;

    public PnToCpogResult(String stdout, VisualCPOG cpog) {
        this.stdout = stdout;
        this.cpog = cpog;
    }

    public String getStdout() {
        return this.stdout;
    }

    public VisualCPOG getCpog() {
        return this.cpog;
    }
}
