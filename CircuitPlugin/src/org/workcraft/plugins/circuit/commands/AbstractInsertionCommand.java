package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractInsertionCommand implements ScriptableCommand<Void> {

    @Override
    public final String getSection() {
        return "Insertion";
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public boolean isVisibleInMenu() {
        return false;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

}
