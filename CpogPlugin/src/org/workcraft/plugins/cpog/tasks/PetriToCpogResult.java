package org.workcraft.plugins.cpog.tasks;

import org.workcraft.plugins.cpog.VisualCpog;

public class PetriToCpogResult {
    private final String stdout;
    private final VisualCpog cpog;

    public PetriToCpogResult(String stdout, VisualCpog cpog) {
        this.stdout = stdout;
        this.cpog = cpog;
    }

    public String getStdout() {
        return this.stdout;
    }

    public VisualCpog getCpog() {
        return this.cpog;
    }
}
