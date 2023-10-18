package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class UscVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Unique State Coding (all cores) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        return new VerificationParameters("Unique state coding",
                VerificationMode.USC_CONFLICT_DETECTION, 0,
                VerificationParameters.SolutionMode.ALL,
                100 /* limit to 100 cores */,
                null, true);
    }

}
