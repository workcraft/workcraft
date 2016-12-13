package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class NormalcyVerificationCommand extends AbstractMpsatVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Normalcy [MPSat]";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, StgModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public MpsatSettings getSettings() {
        return MpsatSettings.getNormalcySettings();
    }

}
