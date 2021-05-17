package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.dialogs.ReachabilityDialog;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.OutcomeUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.AbstractOutputInterpreter;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

class ReachabilityOutputInterpreter extends AbstractOutputInterpreter<MpsatOutput, Boolean> {

    private final ExportOutput exportOutput;
    private final PcompOutput pcompOutput;

    private CompositionData compositionData = null;

    ReachabilityOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(we, mpsatOutput, interactive);
        this.exportOutput = exportOutput;
        this.pcompOutput = pcompOutput;
    }

    public ExportOutput getExportOutput() {
        return exportOutput;
    }

    public PcompOutput getPcompOutput() {
        return pcompOutput;
    }

    public void reportSolutions(String message, List<Solution> solutions) {
        List<Solution> processedSolutions = processSolutions(solutions);
        Framework framework = Framework.getInstance();
        if (isInteractive() && framework.isInGuiMode()) {
            MainWindow mainWindow = framework.getMainWindow();
            ReachabilityDialog solutionsDialog = new ReachabilityDialog(
                    mainWindow, getWorkspaceEntry(), OutcomeUtils.TITLE, message, processedSolutions);

            solutionsDialog.reveal();
        }
    }

    public List<Solution> processSolutions(List<Solution> solutions) {
        List<Solution> result = new LinkedList<>();
        ComponentData data = getComponentData();
        for (Solution solution : solutions) {
            Trace mainTrace = CompositionUtils.projectTrace(solution.getMainTrace(), data);
            Trace branchTrace = CompositionUtils.projectTrace(solution.getBranchTrace(), data);
            String comment = solution.getComment();
            Solution processedSolution = new Solution(mainTrace, branchTrace, comment);
            result.add(processedSolution);
        }
        return result;
    }

    public StgModel getStg() {
        ComponentData data = getComponentData();
        File file = (data != null) ? new File(data.getFileName()) : getOutput().getNetFile();
        return StgUtils.importStg(file);
    }

    public ComponentData getComponentData() {
        CompositionData compositionData = getCompositionData();
        return compositionData == null ? null : compositionData.getComponentData(0);
    }

    public CompositionData getCompositionData() {
        if (getExportOutput() instanceof CompositionExportOutput) {
            return ((CompositionExportOutput) getExportOutput()).getCompositionData();
        }
        if (compositionData == null) {
            if (getPcompOutput() != null) {
                File detailFile = getPcompOutput().getDetailFile();
                try {
                    compositionData = new CompositionData(detailFile);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return compositionData;
    }

    public String getMessage(boolean propertyHolds) {
        return getOutput().getVerificationParameters().getPropertyCheckMessage(propertyHolds);
    }

    public String extendMessage(String message) {
        boolean inversePredicate = getOutput().getVerificationParameters().isInversePredicate();
        String traceCharacteristic = inversePredicate ? "problematic" : "sought";
        String traceInfo = "Trace(s) leading to the " + traceCharacteristic + " state(s):";
        return "<html>&#160;" + message + "<br><br>&#160;" + traceInfo + "<br></html>";
    }

    @Override
    public Boolean interpret() {
        if (getOutput() == null) {
            return null;
        }
        List<Solution> solutions = getOutput().getSolutions();
        boolean predicateSatisfiable = getOutput().hasSolutions();
        boolean inversePredicate = getOutput().getVerificationParameters().isInversePredicate();
        boolean propertyHolds = predicateSatisfiable != inversePredicate;
        String message = getMessage(propertyHolds);
        if (!predicateSatisfiable) {
            OutcomeUtils.showOutcome(propertyHolds, message, isInteractive());
        } else {
            OutcomeUtils.logOutcome(propertyHolds, message);
            reportSolutions(extendMessage(message), solutions);
        }
        return propertyHolds;
    }

}
