package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.Trace;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class MpsatDeadlockFreenessOutputHandler extends MpsatReachabilityOutputHandler {

    MpsatDeadlockFreenessOutputHandler(WorkspaceEntry we,
            ExportOutput exportOutput, PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {

        super(we, exportOutput, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public List<MpsatSolution> processSolutions(WorkspaceEntry we, List<MpsatSolution> solutions) {
        List<MpsatSolution> result = new LinkedList<>();

        StgModel stg = getSrcStg(we);
        ComponentData data = getCompositionData(we);
        Map<String, String> substitutions = getSubstitutions(we);

        for (MpsatSolution solution : solutions) {
            LogUtils.logMessage("Processing reported trace: " + solution.getMainTrace());
            Trace trace = getProjectedTrace(solution.getMainTrace(), data, substitutions);
            // Execute trace to potentially interesting state
            HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
            if (!PetriUtils.fireTrace(stg, trace)) {
                PetriUtils.setMarking(stg, marking);
                throw new RuntimeException("Cannot execute projected trace: " + trace.toText());
            }
            // Check if any output can be fired that is not enabled in the composition
            boolean isConformantTrace = true;
            for (SignalTransition transition : stg.getSignalTransitions()) {
                Signal.Type type = transition.getSignalType();
                if (stg.isEnabled(transition) && (type == Signal.Type.OUTPUT)) {
                    String signal = transition.getSignalName();
                    LogUtils.logWarning("Deadlock trace is spurious because it leads to non-conformant output '" + signal + "'");
                    isConformantTrace = false;
                    break;
                }
            }
            if (isConformantTrace) {
                result.add(new MpsatSolution(trace, null, null));
            }
            PetriUtils.setMarking(stg, marking);
        }
        return result;
    }

    @Override
    public void run() {
        List<MpsatSolution> solutions = getSolutions();
        if (!MpsatUtils.hasTraces(solutions)) {
            DialogUtils.showInfo("The system is deadlock-free", TITLE);
        } else {
            List<MpsatSolution> processedSolutions = processSolutions(getWorkspaceEntry(), solutions);
            if (!MpsatUtils.hasTraces(processedSolutions)) {
                DialogUtils.showWarning("Deadlock freeness cannot be reliably verified because of conformation violation", TITLE);
            } else {
                String message = "The system has a deadlock";
                LogUtils.logWarning(message);
                Framework framework = Framework.getInstance();
                if (framework.isInGuiMode()) {
                    message = "<html><br>&#160;" + message + " after the following trace(s):<br><br></html>";
                    MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(
                            getWorkspaceEntry(), TITLE, message, processedSolutions);
                    MainWindow mainWindow = framework.getMainWindow();
                    GuiUtils.centerToParent(solutionsDialog, mainWindow);
                    solutionsDialog.setVisible(true);
                }
            }
        }
    }

}
