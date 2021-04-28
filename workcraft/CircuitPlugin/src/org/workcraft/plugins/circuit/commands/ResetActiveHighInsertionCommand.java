package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ResetActiveHighInsertionCommand extends AbstractInsertionCommand {

    @Override
    public void insert(WorkspaceEntry we) {
        if (isApplicableTo(we) && VerificationUtils.checkCircuitHasComponents(we)) {
            ResetUtils.insertReset(we, false);
        }
    }

}
