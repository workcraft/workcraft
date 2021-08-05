package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractResetInsertionCommand extends AbstractInsertionCommand {

    @Override
    public void insert(WorkspaceEntry we) {
        if (isApplicableTo(we) && VerificationUtils.checkCircuitHasComponents(we)) {
            VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
            we.captureMemento();
            boolean isModified = ResetUtils.insertReset(circuit, isActiveLow());
            if (isModified) {
                we.saveMemento();
            }
            we.uncaptureMemento();
        }
    }

    abstract boolean isActiveLow();

}
