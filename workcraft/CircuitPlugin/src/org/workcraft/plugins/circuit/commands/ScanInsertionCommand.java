package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ScanInsertionCommand extends AbstractInsertionCommand {

    @Override
    public String getDisplayName() {
        return "Insert scan for path breaker components";
    }

    @Override
    public void insert(WorkspaceEntry we) {
        if (isApplicableTo(we) && VerificationUtils.checkCircuitHasComponents(we)
                && VerificationUtils.checkGateLibrary()) {

            VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
            we.captureMemento();
            boolean isModified = ScanUtils.insertScan(circuit);
            if (isModified) {
                we.saveMemento();
            }
            we.uncaptureMemento();
        }
    }

}
