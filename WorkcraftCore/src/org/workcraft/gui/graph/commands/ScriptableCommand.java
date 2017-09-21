package org.workcraft.gui.graph.commands;

import org.workcraft.workspace.WorkspaceEntry;

public interface ScriptableCommand<T> extends Command {

    T execute(WorkspaceEntry we);

    default void run(WorkspaceEntry we) {
        execute(we);
    }

}
