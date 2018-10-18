package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.tools.Trace;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

class MpsatDeadlockFreenessOutputHandler extends MpsatReachabilityOutputHandler {

    MpsatDeadlockFreenessOutputHandler(WorkspaceEntry we, PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
        super(we, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public List<MpsatSolution> processSolutions(WorkspaceEntry we, List<MpsatSolution> solutions) {
        List<MpsatSolution> result = new LinkedList<>();
        ComponentData data = getCompositionData(we);
        StgModel stg = getSrcStg(data);
        for (MpsatSolution solution : solutions) {
            LogUtils.logMessage("Processing reported trace: " + solution.getMainTrace());
            Trace trace = getProjectedTrace(solution.getMainTrace(), data);
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
                    GUI.centerToParent(solutionsDialog, mainWindow);
                    solutionsDialog.setVisible(true);
                }
            }
        }
    }

}
