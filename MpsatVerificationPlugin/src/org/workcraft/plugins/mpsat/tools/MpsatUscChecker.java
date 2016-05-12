package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatUscChecker extends AbstractMpsatChecker implements Tool {

    @Override
    public String getDisplayName() {
        return "Unique State Coding (all cores) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, StgModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public MpsatSettings getSettings() {
        return MpsatSettings.getUscSettings();
    }

}
