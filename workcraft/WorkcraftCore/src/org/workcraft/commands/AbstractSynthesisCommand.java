package org.workcraft.commands;

import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractSynthesisCommand implements ScriptableCommand<WorkspaceEntry> {

    public static final Category CATEGORY = new Category("Synthesis", 5);

    @Override
    public final Category getCategory() {
        return CATEGORY;
    }

}
