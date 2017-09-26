package org.workcraft.plugins.mpsat;

import java.util.List;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;

final class MpsatDeadlockResultHandler implements Runnable {
    private static final String TITLE = "Verification results";
    private final WorkspaceEntry we;
    private final Result<? extends ExternalProcessResult> result;

    MpsatDeadlockResultHandler(WorkspaceEntry we, Result<? extends ExternalProcessResult> result) {
        this.we = we;
        this.result = result;
    }

    @Override
    public void run() {
        MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue());
        List<MpsatSolution> solutions = mdp.getSolutions();
        if (solutions.isEmpty()) {
            DialogUtils.showInfo("The system is deadlock-free.", TITLE);
        } else if (!MpsatUtils.hasTraces(solutions)) {
            DialogUtils.showWarning("The system has a deadlock.", TITLE);
        } else {
            String message = "<html><br>&#160;The system has a deadlock after the following trace(s):<br><br></html>";
            final MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(we, TITLE, message, solutions);
            MainWindow mainWindow = Framework.getInstance().getMainWindow();
            GUI.centerToParent(solutionsDialog, mainWindow);
            solutionsDialog.setVisible(true);
        }
    }

}
