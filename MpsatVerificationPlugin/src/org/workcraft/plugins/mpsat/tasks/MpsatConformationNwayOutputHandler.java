package org.workcraft.plugins.mpsat.tasks;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

class MpsatConformationNwayOutputHandler extends MpsatConformationOutputHandler {

    private static final String TITLE = "Verification results";

    private final ArrayList<WorkspaceEntry> wes;

    MpsatConformationNwayOutputHandler(ArrayList<WorkspaceEntry> wes,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
        super(wes.get(0), pcompOutput, mpsatOutput, settings);
        this.wes = wes;
    }

    @Override
    public void run() {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        boolean isViolated = false;
        int index = 0;
        for (WorkspaceEntry we: wes) {
            LogUtils.logInfo("Checking conformation for model '" + we.getTitle() + "'");
            List<MpsatSolution> solutions = getSolutions(index++);
            if (!solutions.isEmpty()) {
                isViolated = true;
                if (framework.isInGuiMode()) {
                    mainWindow.requestFocus(we);
                    String message = extendMessage(getMessage(true));
                    String title = TITLE + " for model '" + we.getTitle() + "'";
                    MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(we, title, message, solutions);
                    GUI.centerToParent(solutionsDialog, mainWindow);
                    solutionsDialog.setVisible(true);
                }
            }
        }
        if (!isViolated) {
            String message = getMessage(false);
            DialogUtils.showInfo(message, TITLE);
        }
    }

}
