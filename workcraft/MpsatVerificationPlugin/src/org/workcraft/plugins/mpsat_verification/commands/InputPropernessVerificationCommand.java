package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat_verification.gui.InputPropernessDialog;
import org.workcraft.plugins.mpsat_verification.presets.InputPropernessDataPreserver;
import org.workcraft.plugins.mpsat_verification.presets.InputPropernessParameters;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.List;

public class InputPropernessVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Input properness (without dummies) [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 7;
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
    public void run(WorkspaceEntry we) {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (mainWindow == null) {
            super.run(we);
        } else {
            InputPropernessDataPreserver dataPreserver = new InputPropernessDataPreserver(we);
            InputPropernessDialog dialog = new InputPropernessDialog(mainWindow, dataPreserver);
            if (dialog.reveal()) {
                super.run(we);
            }
        }
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        List<String> data = new InputPropernessDataPreserver(we).loadData();
        return new InputPropernessParameters(data).getVerificationParameters();
    }

}
