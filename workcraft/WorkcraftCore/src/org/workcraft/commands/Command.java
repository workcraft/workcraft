package org.workcraft.commands;

import org.workcraft.workspace.WorkspaceEntry;

public interface Command {
    enum MenuVisibility { NEVER, ACTIVE_APPLICABLE, ALWAYS }

    String getSection();
    String getDisplayName();
    boolean isApplicableTo(WorkspaceEntry we);
    void run(WorkspaceEntry we);

    default MenuVisibility getMenuVisibility() {
        return MenuVisibility.ACTIVE_APPLICABLE;
    }

}
