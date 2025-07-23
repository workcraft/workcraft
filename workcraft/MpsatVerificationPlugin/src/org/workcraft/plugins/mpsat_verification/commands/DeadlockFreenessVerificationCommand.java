package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class DeadlockFreenessVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Deadlock freeness [MPSat]";
    }

    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        return ReachUtils.getDeadlockParameters();
    }

}
