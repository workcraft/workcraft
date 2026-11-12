package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.TimeConsumingCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat_verification.gui.OutputPersistencyDialog;
import org.workcraft.plugins.mpsat_verification.presets.*;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.LinkedList;

public class OutputPersistencyVerificationCommand
        extends AbstractEssentialVerificationCommand
        implements TimeConsumingCommand {

    public static final String TITLE = "Output persistency";

    @Override
    public String getDisplayName() {
        return TITLE + " (without dummies)...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public boolean checkPrerequisites(WorkspaceEntry we) {
        if (!super.checkPrerequisites(we)) {
            return false;
        }
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        if (!stg.getDummyTransitions().isEmpty()) {
            DialogUtils.showError(MpsatUtils.getPropertyUnsupportedDummyWarning(TITLE));
            return false;
        }
        return MutexUtils.mutexStructuralCheck(stg, true);
    }

    @Override
    public void run(WorkspaceEntry we) {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (mainWindow == null) {
            super.run(we);
        } else {
            OutputPersistencyDataPreserver dataPreserver = new OutputPersistencyDataPreserver(we);
            OutputPersistencyDialog dialog = new OutputPersistencyDialog(mainWindow, dataPreserver);
            if (dialog.reveal()) {
                super.run(we);
            }
        }
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        OutputPersistencyParameters data = new OutputPersistencyDataPreserver(we).loadValidDataOnly();
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Pair<String, String>> mutexGrantExceptions = MutexUtils.getMutexGrantPersistencyExceptions(stg);
        return data.getVerificationParameters(mutexGrantExceptions);
    }

}
