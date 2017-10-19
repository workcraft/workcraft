package org.workcraft.commands;

import org.workcraft.workspace.WorkspaceEntry;

public interface ScriptableCommand<T> extends Command {

    @Override
    default void run(WorkspaceEntry we) {
        execute(we);
    }

    T execute(WorkspaceEntry we);

}
