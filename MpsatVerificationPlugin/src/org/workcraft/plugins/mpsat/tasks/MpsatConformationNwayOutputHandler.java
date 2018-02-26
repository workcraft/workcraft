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
        boolean violated = false;
        int index = 0;
        for (WorkspaceEntry we: wes) {
            LogUtils.logInfo("Applying solutions to '" + we.getTitle() + "'");
            List<MpsatSolution> solutions = getSolutions(index++);
            if (!solutions.isEmpty()) {
                violated = true;
                if (framework.isInGuiMode()) {
                    mainWindow.requestFocus(we);
                    String message = getMessage(true);
                    message = extendMessage(message);
                    MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(we, TITLE, message, solutions);
                    GUI.centerToParent(solutionsDialog, mainWindow);
                    solutionsDialog.setVisible(true);
                }
            }
        }
        if (!violated) {
            String message = getMessage(false);
            DialogUtils.showInfo(message, TITLE);
        }
    }

}
