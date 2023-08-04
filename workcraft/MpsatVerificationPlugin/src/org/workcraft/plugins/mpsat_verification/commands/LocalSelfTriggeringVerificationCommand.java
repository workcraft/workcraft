package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class LocalSelfTriggeringVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Absence of self-triggering local signals (without dummies) [MPSat]";
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
        return ReachUtils.getNoLocalSelfTriggeringParameters();
    }

}
