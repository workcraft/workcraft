package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class ConsistencyVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Consistency [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 6;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        return ReachUtils.getConsistencyParameters();
    }

}
