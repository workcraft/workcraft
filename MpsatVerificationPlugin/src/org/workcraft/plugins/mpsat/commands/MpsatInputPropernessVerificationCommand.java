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
        boolean result = super.checkPrerequisites(we);
        if (result) {
            StgModel stg = WorkspaceUtils.getAs(we, StgModel.class);
            if (!stg.getDummyTransitions().isEmpty()) {
                DialogUtils.showError("Input properness can currently be checked only for STGs without dummies.");
                result = false;
            }
        }
        return result;
    }

    @Override
    public MpsatParameters getSettings(WorkspaceEntry we) {
        return MpsatParameters.getInputPropernessSettings();
    }

}
