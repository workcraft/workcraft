package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class CircuitLayoutRoutingCommand extends CircuitLayoutCommand {

    @Override
    public String getDisplayName() {
        return "Circuit routing only";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean skipLayoutPlacement() {
        return true;
    }

}
