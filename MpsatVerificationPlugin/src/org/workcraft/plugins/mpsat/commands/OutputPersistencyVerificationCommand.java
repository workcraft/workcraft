package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class OutputPersistencyVerificationCommand extends AbstractMpsatVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Output persistency (without dummies) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public MpsatSettings getSettings() {
        return MpsatSettings.getOutputPersistencySettings();
    }

}
