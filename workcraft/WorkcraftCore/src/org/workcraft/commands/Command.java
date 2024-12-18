package org.workcraft.commands;

import org.workcraft.workspace.WorkspaceEntry;

public interface Command {
    enum MenuVisibility { NEVER, APPLICABLE, ALWAYS, APPLICABLE_POPUP_ONLY }

    String getSection();
    String getDisplayName();
    boolean isApplicableTo(WorkspaceEntry we);
    void run(WorkspaceEntry we);

    default MenuVisibility getMenuVisibility() {
        return MenuVisibility.APPLICABLE;
    }

}
