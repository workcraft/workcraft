package org.workcraft.commands;

import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.workspace.WorkspaceEntry;

public interface DataCommand<D> extends Command {
    void run(WorkspaceEntry we, D data, ProgressMonitor monitor);
    D deserialiseData(String data);
}
