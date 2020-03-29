package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class DeadlockFreenessVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Deadlock freeness [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public int getPriority() {
        return 5;
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
