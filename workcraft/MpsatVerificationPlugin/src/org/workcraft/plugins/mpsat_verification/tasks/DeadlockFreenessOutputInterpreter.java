package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.dialogs.ReachibilityDialog;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TraceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class DeadlockFreenessOutputInterpreter extends ReachabilityOutputInterpreter {

    DeadlockFreenessOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(we, exportOutput, pcompOutput, mpsatOutput, interactive);
    }

    @Override
    public List<Solution> processSolutions(WorkspaceEntry we, List<Solution> solutions) {
        List<Solution> result = new LinkedList<>();

        StgModel stg = getSrcStg(we);
        ComponentData data = getCompositionData(we);
        Map<String, String> substitutions = getSubstitutions(we);

        for (Solution solution : solutions) {
            LogUtils.logMessage("Processing reported trace: " + solution.getMainTrace());
            Trace trace = getProjectedTrace(solution.getMainTrace(), data, substitutions);
            // Execute trace to potentially interesting state
            HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
            if (!PetriUtils.fireTrace(stg, trace)) {
                PetriUtils.setMarking(stg, marking);
                throw new RuntimeException("Cannot execute projected trace: " + trace.toString());
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
                result.add(new Solution(trace));
            }
            PetriUtils.setMarking(stg, marking);
        }
        return result;
    }

    @Override
    public Boolean interpret() {
        if (getOutput() == null) {
            return null;
        }
        List<Solution> solutions = getSolutions();
        boolean propertyHolds = !TraceUtils.hasTraces(solutions);
        if (propertyHolds) {
            showOutcome(propertyHolds, "The system is deadlock-free");
        } else {
            List<Solution> processedSolutions = processSolutions(getWorkspaceEntry(), solutions);
            if (!TraceUtils.hasTraces(processedSolutions)) {
                showOutcome(propertyHolds, "Deadlock freeness cannot be reliably verified because of conformation violation");
                return null;
            } else {
                String message = "The system has a deadlock";
                LogUtils.logWarning(message);
                if (isInteractive()) {
                    message = "<html><br>&#160;" + message + " after the following trace(s):<br><br></html>";
                    MainWindow mainWindow = Framework.getInstance().getMainWindow();
                    ReachibilityDialog solutionsDialog = new ReachibilityDialog(
                            mainWindow, getWorkspaceEntry(), TITLE, message, processedSolutions);

                    solutionsDialog.reveal();
                }
            }
        }
        return propertyHolds;
    }

}
