package org.workcraft.plugins.circuit.commands;

import org.workcraft.MenuOrdering;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class CircuitAbstractInitialisationCommand implements ScriptableCommand<Void>, MenuOrdering {

    @Override
    public final String getSection() {
        return "Initialisation";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

}
