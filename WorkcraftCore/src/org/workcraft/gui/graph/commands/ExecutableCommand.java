package org.workcraft.gui.graph.commands;

import org.workcraft.workspace.WorkspaceEntry;

public interface ExecutableCommand extends Command {
    WorkspaceEntry execute(WorkspaceEntry we);
}
