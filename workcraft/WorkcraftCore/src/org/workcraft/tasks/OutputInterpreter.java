package org.workcraft.tasks;

import org.workcraft.workspace.WorkspaceEntry;

public interface OutputInterpreter<T extends ExternalProcessOutput, U> {
    WorkspaceEntry getWorkspaceEntry();
    T getOutput();
    boolean isInteractive();
    U interpret();
}
