package org.workcraft.commands;

import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractSynthesisCommand implements ScriptableCommand<WorkspaceEntry>, MenuOrdering {

    @Override
    public final String getSection() {
        return "! Synthesis";  // 1 space - positions 4th
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return null;
    }

}
