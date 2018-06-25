package org.workcraft.plugins.mpsat.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.gui.graph.tools.Trace;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.PetriUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

class MpsatConformationOutputHandler extends MpsatReachabilityOutputHandler {

    private HashMap<MpsatSolution, HashSet<String>> solutionToEnableSignals = null;

    MpsatConformationOutputHandler(WorkspaceEntry we, PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
        super(we, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public List<MpsatSolution> processSolutions(WorkspaceEntry we, List<MpsatSolution> solutions) {
        List<MpsatSolution> result = new LinkedList<>();
        HashMap<MpsatSolution, HashSet<String>> solutionToEnableSignals = getSolutionToEnableSignalsMap(solutions);
        ComponentData data = getCompositionData(we);
        HashSet<String> visitedTraces = new HashSet<>();
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Unique projection(s) to '" + we.getTitle() + "':");
        }
        StgModel stg = getSrcStg(data);
        for (MpsatSolution solution: solutions) {
            // Get unique projection trace
            Trace trace = getProjectedTrace(solution.getMainTrace(), data);
            String traceText = trace.toText();
            if (visitedTraces.contains(traceText)) continue;
            visitedTraces.add(traceText);
            if (needsMultiLineMessage) {
                LogUtils.logMessage("  " + traceText);
            } else {
                LogUtils.logMessage("Projection to '" + we.getTitle() + "': " + traceText);
            }
            // Execute trace to potentially interesting state
            HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
            if (!PetriUtils.fireTrace(stg, trace)) {
                PetriUtils.setMarking(stg, marking);
                throw new RuntimeException("Cannot execute projected trace: " + traceText);
            }
            // Check if any output can be fired that is not enabled in the composition
            HashSet<String> compEnabledSignals = solutionToEnableSignals.get(solution);
            for (SignalTransition transition: stg.getSignalTransitions()) {
                Signal.Type type = transition.getSignalType();
                String signal = transition.getSignalName();
                if (stg.isEnabled(transition) && (type == Signal.Type.OUTPUT) && !compEnabledSignals.contains(signal)) {
                    String ref = stg.getNodeReference(transition);
                    LogUtils.logWarning("Output '" + ref + "' becomes unexpectedly enabled");
                    trace.add(stg.getNodeReference(transition));
                    String comment = "Unexpected change of output '" + signal + "'";
                    MpsatSolution processedSolution = new MpsatSolution(trace, null, comment);
                    result.add(processedSolution);
                    break;
                }
            }
            PetriUtils.setMarking(stg, marking);
        }
        return result;
    }

    private HashMap<MpsatSolution, HashSet<String>> getSolutionToEnableSignalsMap(List<MpsatSolution> solutions) {
        if (solutionToEnableSignals == null) {
            solutionToEnableSignals = new HashMap<>();
            StgModel compStg = getMpsatOutput().getInputStg();
            boolean needsMultiLineMessage = solutions.size() > 1;
            if (needsMultiLineMessage) {
                LogUtils.logMessage("Violation traces of the composition:");
            }
            for (MpsatSolution solution: solutions) {
                Trace trace = solution.getMainTrace();
                if (needsMultiLineMessage) {
                    LogUtils.logMessage("  " + trace.toText());
                } else {
                    LogUtils.logMessage("Violation trace of the composition: " + trace.toText());
                }
                HashSet<String> enableSignals = getEnableSignalsAfterTrace(compStg, trace);
                solutionToEnableSignals.put(solution, enableSignals);
            }
        }
        return solutionToEnableSignals;
    }

    private HashSet<String> getEnableSignalsAfterTrace(StgModel stg, Trace trace) {
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        if (!PetriUtils.fireTrace(stg, trace)) {
            PetriUtils.setMarking(stg, marking);
            throw new RuntimeException("Cannot execute trace: " + trace.toText());
        }
        HashSet<String> result = StgUtils.getEnabledSignals(stg);
        PetriUtils.setMarking(stg, marking);
        return result;
    }

}
