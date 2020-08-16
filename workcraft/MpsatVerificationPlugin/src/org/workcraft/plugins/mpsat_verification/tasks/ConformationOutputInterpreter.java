package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.utils.EnablednessUtils;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
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
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

class ConformationOutputInterpreter extends AbstractCompositionOutputInterpreter {

    ConformationOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(we, exportOutput, pcompOutput, mpsatOutput, interactive);
    }

    @Override
    public StgModel getStg() {
        // 1-way conformation for STG uses composition of *modified* STG components (internal signals
        // are replaced with dummies), therefore original STG should be obtained from WorkspaceEntries.
        WorkspaceEntry we = getWorkspaceEntry();
        if (WorkspaceUtils.isApplicable(we, StgModel.class)) {
            ModelEntry me = WorkUtils.cloneModel(we.getModelEntry());
            return WorkspaceUtils.getAs(me, StgModel.class);
        }
        // Conformation of a circuit to its environment uses unmodified device STG that can be
        // obtained from composition data as implemented in ReachabilityOutputInterpreter.
        return super.getStg();
    }

    @Override
    public List<Solution> processSolutions(List<Solution> solutions) {
        String title = getWorkspaceEntry().getTitle();
        StgModel stg = getStg();
        ComponentData data = getComponentData();
        Map<String, String> substitutions = getSubstitutions();
        return processSolutions(solutions, title, stg, data, substitutions);
    }

    public List<Solution> processSolutions(List<Solution> solutions, String title, StgModel stg,
            ComponentData data, Map<String, String> substitutions) {

        List<Solution> result = new LinkedList<>();
        HashSet<String> visitedTraces = new HashSet<>();
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Unique projection(s) to '" + title + "':");
        }

        StgModel compStg = getOutput().getInputStg();
        for (Solution solution : solutions) {
            // Get unique projection trace
            Trace trace = getProjectedTrace(solution.getMainTrace(), data, substitutions);
            String traceText = trace.toString();
            if (!visitedTraces.contains(traceText)) {
                visitedTraces.add(traceText);
                if (needsMultiLineMessage) {
                    LogUtils.logMessage("  " + traceText);
                } else {
                    LogUtils.logMessage("Projection to '" + title + "': " + traceText);
                }
                Trace compTrace = MpsatUtils.fixTraceToggleEvents(compStg, solution.getMainTrace());
                Enabledness compEnabledness = EnablednessUtils.getEnablednessAfterTrace(compStg, compTrace);
                Solution processedSolution = processSolution(stg, trace, compEnabledness);
                if (processedSolution != null) {
                    result.add(processedSolution);
                }
            }
        }
        return result;
    }

    private Solution processSolution(StgModel stg, Trace trace, Enabledness compEnabledness) {
        // Execute trace to a potentially problematic state
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        if (!PetriUtils.fireTrace(stg, trace)) {
            PetriUtils.setMarking(stg, marking);
            throw new RuntimeException("Cannot execute projected trace: " + trace.toString());
        }
        // Check if any output can be fired that is not enabled in the composition STG

        // Find enabled signals whose state is unknown (due to dummies) in the composition STG.
        HashSet<String> suspiciousSignals = EnablednessUtils.getEnabledSignals(stg, Signal.Type.OUTPUT);
        suspiciousSignals.removeAll(compEnabledness.getEnabledSet());
        suspiciousSignals.removeAll(compEnabledness.getDisabledSet());
        if (suspiciousSignals.size() == 1) {
            // If there is only one such signal, then it is actually the one disabled in the composition STG.
            compEnabledness.disable(suspiciousSignals);
        }
        // Find an enabled transition that is definitely disabled in composition STG.
        SignalTransition problematicTransition = null;
        for (SignalTransition transition: stg.getSignalTransitions(Signal.Type.OUTPUT)) {
            String signalRef = stg.getSignalReference(transition);
            if (stg.isEnabled(transition) && compEnabledness.isDisabled(signalRef)) {
                problematicTransition = transition;
                break;
            }
        }

        Solution processedSolution = null;
        if (problematicTransition != null) {
            // If the problematic transition found, add it to the trace.
            String ref = stg.getSignalReference(problematicTransition) + problematicTransition.getDirection();
            LogUtils.logWarning("Output '" + ref + "' becomes unexpectedly enabled");
            trace.add(stg.getNodeReference(problematicTransition));
            String comment = "Unexpected change of output '" + ref + "'";
            processedSolution = new Solution(trace, null, comment);
        } else if (!suspiciousSignals.isEmpty()) {
            // Otherwise add all disabled signals to the trace description.
            String refs = String.join(", ", suspiciousSignals);
            LogUtils.logWarning("One of these outputs becomes unexpectedly enabled (via internal signals or dummies):\n" + refs);
            String comment = "Unexpected enabling of one of the outputs: " + refs;
            processedSolution = new Solution(trace, null, comment);
        }

        PetriUtils.setMarking(stg, marking);
        // Note that if no violating transitions found, then this component does not break conformation -- return null
        return processedSolution;
    }

}
