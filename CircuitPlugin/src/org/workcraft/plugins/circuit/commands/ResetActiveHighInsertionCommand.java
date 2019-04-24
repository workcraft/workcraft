package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ResetActiveHighInsertionCommand extends AbstractInsertionCommand {

    @Override
    public Void execute(WorkspaceEntry we) {
        if (isApplicableTo(we) && VerificationUtils.checkCircuitHasComponents(we)) {
            we.saveMemento();
            VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
            ResetUtils.insertReset(circuit, CircuitSettings.getResetPort(), false);
        }
        return null;
    }

}
