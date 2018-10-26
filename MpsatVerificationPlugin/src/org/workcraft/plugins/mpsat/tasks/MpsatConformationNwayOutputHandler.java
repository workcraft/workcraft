package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MpsatConformationNwayOutputHandler extends MpsatConformationOutputHandler {

    private final ArrayList<WorkspaceEntry> wes;

    MpsatConformationNwayOutputHandler(ArrayList<WorkspaceEntry> wes, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {

        super(wes.get(0), exportOutput, pcompOutput, mpsatOutput, settings);
        this.wes = wes;
    }

    @Override
    public Map<String, String> getSubstitutions(WorkspaceEntry we) {
        int index = wes.indexOf(we);
        return getSubstitutions(index);
    }

    @Override
    public ComponentData getCompositionData(WorkspaceEntry we) {
        int index = wes.indexOf(we);
        return getCompositionData(index);
    }

    @Override
    public StgModel getSrcStg(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        ModelEntry me = framework.cloneModel(we.getModelEntry());
        return WorkspaceUtils.getAs(me, StgModel.class);
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
