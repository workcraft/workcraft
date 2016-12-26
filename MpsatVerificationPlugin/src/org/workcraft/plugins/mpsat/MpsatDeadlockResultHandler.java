package org.workcraft.plugins.mpsat;

import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.mpsat.gui.MpsatSolution;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;
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
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (solutions.isEmpty()) {
            String message = "The system is deadlock-free.";
            JOptionPane.showMessageDialog(mainWindow, message, TITLE, JOptionPane.INFORMATION_MESSAGE);
        } else if (!MpsatSolution.hasTraces(solutions)) {
            String message = "The system has a deadlock.";
            JOptionPane.showMessageDialog(mainWindow, message, TITLE, JOptionPane.WARNING_MESSAGE);
        } else {
            String message = "<html><br>&#160;The system has a deadlock after the following trace(s):<br><br></html>";
            final MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(we, TITLE, message, solutions);
            GUI.centerToParent(solutionsDialog, mainWindow);
            solutionsDialog.setVisible(true);
        }
    }

}
