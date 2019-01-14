package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatInputPropernessVerificationCommand extends MpsatAbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Input properness (without dummies) [MPSat]";
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
        return Position.TOP;
    }

    @Override
    public boolean checkPrerequisites(WorkspaceEntry we) {
        if (!super.checkPrerequisites(we)) {
            return false;
        }
        StgModel stg = WorkspaceUtils.getAs(we, StgModel.class);
        if (!stg.getDummyTransitions().isEmpty()) {
            DialogUtils.showError("Input properness can currently be checked only for STGs without dummies.");
            return false;
        }
        return true;
    }

    @Override
    public MpsatParameters getSettings(WorkspaceEntry we) {
        return MpsatParameters.getInputPropernessSettings();
    }

}
