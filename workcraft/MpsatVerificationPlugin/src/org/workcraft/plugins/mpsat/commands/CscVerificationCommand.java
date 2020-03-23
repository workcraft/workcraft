package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CscVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Complete State Coding (all cores) [MPSat]";
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
        return new VerificationParameters("Complete state coding",
                VerificationMode.CSC_CONFLICT_DETECTION, 0,
                VerificationParameters.SolutionMode.ALL,
                -1 /* unlimited */,
                null, true);
    }

}
