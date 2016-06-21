package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatNormalcyChecker extends AbstractMpsatChecker {

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
        return Position.TOP;
    }

    @Override
    public MpsatSettings getSettings() {
        return MpsatSettings.getNormalcySettings();
    }

}
