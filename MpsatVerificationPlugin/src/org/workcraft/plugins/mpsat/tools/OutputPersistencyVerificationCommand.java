package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class OutputPersistencyVerificationCommand extends AbstractMpsatVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Output persistency (without dummies) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, StgModel.class);
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
