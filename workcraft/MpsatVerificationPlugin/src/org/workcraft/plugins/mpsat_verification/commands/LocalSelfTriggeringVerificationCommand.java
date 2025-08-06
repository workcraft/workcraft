package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat_verification.gui.LocalSelfTriggeringDialog;
import org.workcraft.plugins.mpsat_verification.presets.LocalSelfTriggeringDataPreserver;
import org.workcraft.plugins.mpsat_verification.presets.LocalSelfTriggeringParameters;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;

public class LocalSelfTriggeringVerificationCommand
        extends AbstractEssentialVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Absence of local self-triggering (without dummies)...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public int getPriority() {
        return 40;
    }

    @Override
    public boolean checkPrerequisites(WorkspaceEntry we) {
        if (!super.checkPrerequisites(we)) {
            return false;
        }
        StgModel stg = WorkspaceUtils.getAs(we, StgModel.class);
        if (!stg.getDummyTransitions().isEmpty()) {
            DialogUtils.showError("Absence of local self-triggering can currently be checked only for STGs without dummies.");
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
            LocalSelfTriggeringDataPreserver dataPreserver = new LocalSelfTriggeringDataPreserver(we);
            LocalSelfTriggeringDialog dialog = new LocalSelfTriggeringDialog(mainWindow, dataPreserver);
            if (dialog.reveal()) {
                super.run(we);
            }
        }
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        Collection<String> data = new LocalSelfTriggeringDataPreserver(we).loadData();
        return new LocalSelfTriggeringParameters(data).getVerificationParameters();
    }

}
