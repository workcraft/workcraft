package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ScanInsertionCommand extends AbstractInsertionCommand {

    @Override
    public String getDisplayName() {
        return "Insert scan for path breaker components";
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        we.saveMemento();
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        ScanUtils.insertScan(circuit);
        return null;
    }

}
