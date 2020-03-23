package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class InputPropernessVerificationCommand extends AbstractVerificationCommand {

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
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        return ReachUtils.getInputPropernessSettings();
    }

}
