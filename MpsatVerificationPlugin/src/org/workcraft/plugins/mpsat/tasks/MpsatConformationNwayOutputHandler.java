package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.List;

class MpsatConformationNwayOutputHandler extends MpsatConformationOutputHandler {

    private final ArrayList<WorkspaceEntry> wes;

    MpsatConformationNwayOutputHandler(ArrayList<WorkspaceEntry> wes,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
        super(wes.get(0), pcompOutput, mpsatOutput, settings);
        this.wes = wes;
    }

    @Override
    public ComponentData getCompositionData(WorkspaceEntry we) {
        int index = wes.indexOf(we);
        return getCompositionData(index);
    }

    @Override
    public void run() {
        List<MpsatSolution> solutions = getSolutions();
        boolean isConformant = solutions.isEmpty();
        String message = getMessage(!isConformant);
        if (isConformant) {
            DialogUtils.showInfo(message, TITLE);
        } else {
            LogUtils.logWarning(message);
            Framework framework = Framework.getInstance();
            MainWindow mainWindow = framework.getMainWindow();
            for (WorkspaceEntry we: wes) {
                List<MpsatSolution> processedSolutions = processSolutions(we, solutions);
                if (!processedSolutions.isEmpty() && framework.isInGuiMode()) {
                    mainWindow.requestFocus(we);
                    String title = TITLE + " for model '" + we.getTitle() + "'";
                    String extendedMessage = extendMessage(message);
                    MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(
                            we, title, extendedMessage, processedSolutions);
                    GUI.centerToParent(solutionsDialog, mainWindow);
                    solutionsDialog.setVisible(true);
                }
            }
        }
    }

}
