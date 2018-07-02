package org.workcraft.commands;

import org.workcraft.workspace.WorkspaceEntry;

public interface Command {
    String getSection();
    String getDisplayName();
    boolean isApplicableTo(WorkspaceEntry we);
    void run(WorkspaceEntry we);

    default boolean isVisibleInMenu() {
        return true;
    }
}
