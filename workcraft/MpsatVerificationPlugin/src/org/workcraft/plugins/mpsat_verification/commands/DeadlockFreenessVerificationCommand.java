package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.commands.TimeConsumingCommand;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class DeadlockFreenessVerificationCommand
        extends AbstractEssentialVerificationCommand
        implements TimeConsumingCommand {

    @Override
    public String getDisplayName() {
        return "Deadlock freeness";
    }

    @Override
    public int getPriority() {
        return 30;
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        return ReachUtils.getDeadlockParameters();
    }

}
