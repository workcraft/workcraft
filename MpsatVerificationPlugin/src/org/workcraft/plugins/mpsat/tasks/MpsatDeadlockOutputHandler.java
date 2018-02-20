package org.workcraft.plugins.mpsat.tasks;

import java.util.List;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

final class MpsatDeadlockOutputHandler implements Runnable {
    private static final String TITLE = "Verification results";
    private final WorkspaceEntry we;
    private final MpsatOutput output;

    MpsatDeadlockOutputHandler(WorkspaceEntry we, MpsatOutput output) {
        this.we = we;
        this.output = output;
    }

    @Override
    public void run() {
        Framework framework = Framework.getInstance();
        MpsatOutoutParser mdp = new MpsatOutoutParser(output);
        List<MpsatSolution> solutions = mdp.getSolutions();
        if (solutions.isEmpty()) {
            DialogUtils.showInfo("The system is deadlock-free.", TITLE);
        } else if (!MpsatUtils.hasTraces(solutions)) {
            DialogUtils.showWarning("The system has a deadlock.", TITLE);
        } else if (framework.isInGuiMode()) {
            String message = "<html><br>&#160;The system has a deadlock after the following trace(s):<br><br></html>";
            MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(we, TITLE, message, solutions);
            MainWindow mainWindow = framework.getMainWindow();
            GUI.centerToParent(solutionsDialog, mainWindow);
            solutionsDialog.setVisible(true);
        }
    }

}
