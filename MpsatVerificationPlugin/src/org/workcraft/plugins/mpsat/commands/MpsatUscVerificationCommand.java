package org.workcraft.plugins.mpsat.commands;

import org.workcraft.commands.Command;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class MpsatUscVerificationCommand extends MpsatAbstractVerificationCommand implements Command {

    @Override
    public String getDisplayName() {
        return "Unique State Coding (all cores) [MPSat]";
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
    public MpsatParameters getSettings(WorkspaceEntry we) {
        return MpsatParameters.getUscSettings();
    }

}
