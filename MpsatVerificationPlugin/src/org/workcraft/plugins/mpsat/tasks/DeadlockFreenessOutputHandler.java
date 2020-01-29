package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.simulation.ReachibilityDialog;
import org.workcraft.gui.simulation.SimulationUtils;
import org.workcraft.gui.simulation.Solution;
import org.workcraft.gui.simulation.Trace;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class DeadlockFreenessOutputHandler extends ReachabilityOutputHandler {

    DeadlockFreenessOutputHandler(WorkspaceEntry we,
            ExportOutput exportOutput, PcompOutput pcompOutput, VerificationOutput mpsatOutput, VerificationParameters settings) {

        super(we, exportOutput, pcompOutput, mpsatOutput, settings);
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
                result.add(new Solution(trace, null, null));
            }
            PetriUtils.setMarking(stg, marking);
        }
        return result;
    }

    @Override
    public void run() {
        List<Solution> solutions = getSolutions();
        if (!SimulationUtils.hasTraces(solutions)) {
            DialogUtils.showInfo("The system is deadlock-free", TITLE);
        } else {
            List<Solution> processedSolutions = processSolutions(getWorkspaceEntry(), solutions);
            if (!SimulationUtils.hasTraces(processedSolutions)) {
                DialogUtils.showWarning("Deadlock freeness cannot be reliably verified because of conformation violation", TITLE);
            } else {
                String message = "The system has a deadlock";
                LogUtils.logWarning(message);
                Framework framework = Framework.getInstance();
                if (framework.isInGuiMode()) {
                    message = "<html><br>&#160;" + message + " after the following trace(s):<br><br></html>";
                    ReachibilityDialog solutionsDialog = new ReachibilityDialog(
                            framework.getMainWindow(), getWorkspaceEntry(), TITLE, message, processedSolutions);

                    solutionsDialog.reveal();
                }
            }
        }
    }

}
