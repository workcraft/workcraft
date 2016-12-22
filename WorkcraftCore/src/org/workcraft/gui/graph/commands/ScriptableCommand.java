package org.workcraft.gui.graph.commands;

import org.workcraft.workspace.WorkspaceEntry;

public interface ScriptableCommand extends Command {
    WorkspaceEntry execute(WorkspaceEntry we);
}
