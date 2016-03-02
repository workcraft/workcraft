package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatInputPropernessChecker extends AbstractMpsatChecker {

    @Override
    public String getDisplayName() {
        return "Input properness (without dummies) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, STGModel.class);
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public MpsatSettings getSettings() {
        return MpsatSettings.getInputPropernessSettings();
    }

}
