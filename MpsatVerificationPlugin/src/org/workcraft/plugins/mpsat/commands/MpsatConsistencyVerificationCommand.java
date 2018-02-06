package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatConsistencyVerificationCommand extends MpsatAbstractVerificationCommand {

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
        return null;
    }

    @Override
    public MpsatParameters getSettings(WorkspaceEntry we) {
        return MpsatParameters.getConsistencySettings();
    }

}
