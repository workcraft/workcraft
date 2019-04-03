package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class NormalcyVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Normalcy [MPSat]";
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
    public VerificationParameters getSettings(WorkspaceEntry we) {
        return VerificationParameters.getNormalcySettings();
    }

}
