package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TestableGateInsertionCommand extends AbstractInsertionCommand {

    @Override
    public String getDisplayName() {
        return "Insert testable buffers/inverters for path breaker components";
    }

    @Override
    public void run(WorkspaceEntry we) {
        we.saveMemento();
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        ScanUtils.insertTestableGates(circuit);
    }

}
