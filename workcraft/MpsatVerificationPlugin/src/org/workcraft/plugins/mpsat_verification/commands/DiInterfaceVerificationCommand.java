package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat_verification.gui.DiInterfaceDialog;
import org.workcraft.plugins.mpsat_verification.presets.DiInterfaceDataPreserver;
import org.workcraft.plugins.mpsat_verification.presets.DiInterfaceParameters;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class DiInterfaceVerificationCommand
        extends AbstractEssentialVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Delay insensitive interface (without dummies)...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public boolean checkPrerequisites(WorkspaceEntry we) {
        if (!super.checkPrerequisites(we)) {
            return false;
        }
        StgModel stg = WorkspaceUtils.getAs(we, StgModel.class);
        if (!stg.getDummyTransitions().isEmpty()) {
            DialogUtils.showError("Delay insensitive interface can currently be checked only for STGs without dummies.");
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
            DiInterfaceDataPreserver dataPreserver = new DiInterfaceDataPreserver(we);
            DiInterfaceDialog dialog = new DiInterfaceDialog(mainWindow, dataPreserver);
            if (dialog.reveal()) {
                super.run(we);
            }
        }
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        DiInterfaceParameters data = new DiInterfaceDataPreserver(we).loadData();
        return data.getVerificationParameters();
    }

}
