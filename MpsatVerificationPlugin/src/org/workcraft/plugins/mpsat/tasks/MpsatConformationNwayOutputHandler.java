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
        int index = 0;
        for (WorkspaceEntry we: wes) {
            LogUtils.logInfo(we.getWorkspacePath().toString());
            List<MpsatSolution> solutions = getSolutions(index++);
            String message = getMessage(!solutions.isEmpty());
            Framework framework = Framework.getInstance();
            if (!MpsatUtils.hasTraces(solutions)) {
                DialogUtils.showInfo(message, TITLE);
            } else if (framework.isInGuiMode()) {
                message = extendMessage(message);
                MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(getWorkspaceEntry(), TITLE, message, solutions);
                MainWindow mainWindow = framework.getMainWindow();
                GUI.centerToParent(solutionsDialog, mainWindow);
                solutionsDialog.setVisible(true);
            }
        }
    }

}
