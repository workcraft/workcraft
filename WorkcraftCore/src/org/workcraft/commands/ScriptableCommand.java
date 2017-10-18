package org.workcraft.commands;

import org.workcraft.workspace.WorkspaceEntry;

public interface ScriptableCommand<T> extends Command {
    T execute(WorkspaceEntry we);
}
