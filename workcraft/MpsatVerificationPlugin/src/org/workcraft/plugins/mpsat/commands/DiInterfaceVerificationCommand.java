package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class DiInterfaceVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Delay insensitive interface (without dummies) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        return ReachUtils.getDiInterfaceParameters();
    }

}
