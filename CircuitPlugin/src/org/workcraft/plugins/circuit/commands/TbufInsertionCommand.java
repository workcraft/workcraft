package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TbufInsertionCommand extends AbstractInsertionCommand {

    @Override
    public String getDisplayName() {
        return "Insert testable buffers for path breaker components";
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        if (ScanUtils.check(circuit.getMathModel())) {
            we.saveMemento();
            ScanUtils.insertTestableBuffers(circuit);
        }
        return null;
    }

}
