package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.MpsatSettings;
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
        return 5;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public MpsatSettings getSettings() {
        return MpsatSettings.getConsistencySettings();
    }

}
