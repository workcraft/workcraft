package org.workcraft.commands;

import org.workcraft.workspace.WorkspaceEntry;

public interface ScriptableDataCommand<T, D> extends DataCommand<D> {
    T execute(WorkspaceEntry we, D data);
}
