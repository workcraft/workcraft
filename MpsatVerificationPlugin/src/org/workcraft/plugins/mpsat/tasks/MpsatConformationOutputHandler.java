package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.gui.graph.tools.Trace;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.utils.EnablednessUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

class MpsatConformationOutputHandler extends MpsatReachabilityOutputHandler {

    MpsatConformationOutputHandler(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {

        super(we, exportOutput, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public void reportSolutions(List<MpsatSolution> solutions) {
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
        }
    }

    @Override
    public List<MpsatSolution> processSolutions(WorkspaceEntry we, List<MpsatSolution> solutions) {
        List<MpsatSolution> result = new LinkedList<>();

        StgModel stg = getSrcStg(we);
        ComponentData data = getCompositionData(we);
        Map<String, String> substitutions = getSubstitutions(we);

        HashSet<String> visitedTraces = new HashSet<>();
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Unique projection(s) to '" + we.getTitle() + "':");
        }

        StgModel compStg = getMpsatOutput().getInputStg();
        HashMap<MpsatSolution, Enabledness> solutionToCompEnabledness = getSolutionToEnabledness(compStg, solutions);

        for (MpsatSolution solution: solutions) {
            // Get unique projection trace
            Trace trace = getProjectedTrace(solution.getMainTrace(), data, substitutions);
            String traceText = trace.toText();
            if (visitedTraces.contains(traceText)) continue;
            visitedTraces.add(traceText);

            if (needsMultiLineMessage) {
                LogUtils.logMessage("  " + traceText);
            } else {
                LogUtils.logMessage("Projection to '" + we.getTitle() + "': " + traceText);
            }

            Enabledness compEnabledness = solutionToCompEnabledness.get(solution);
            MpsatSolution processedSolution = processSolution(stg, trace, compEnabledness);
            if (processedSolution != null) {
                result.add(processedSolution);
            }
        }
        return result;
    }

    private MpsatSolution processSolution(StgModel stg, Trace trace, Enabledness compEnabledness) {
        // Execute trace to potentially interesting state
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        if (!PetriUtils.fireTrace(stg, trace)) {
            PetriUtils.setMarking(stg, marking);
            throw new RuntimeException("Cannot execute projected trace: " + trace.toText());
        }
        // Check if any output can be fired that is not enabled in the composition
        HashSet<String> suspiciousSignals = EnablednessUtils.getEnabledSignals(stg, Signal.Type.OUTPUT);
        suspiciousSignals.retainAll(compEnabledness.getUnknownSet());
        if (suspiciousSignals.size() == 1) {
            compEnabledness.alter(Collections.emptySet(), suspiciousSignals, Collections.emptySet());
        }

        SignalTransition problematicTransition = null;
        for (SignalTransition transition: stg.getSignalTransitions(Signal.Type.OUTPUT)) {
            String signalRef = stg.getSignalReference(transition);
            if (stg.isEnabled(transition) && compEnabledness.isDisabled(signalRef)) {
                problematicTransition = transition;
                break;
            }
        }
        MpsatSolution processedSolution = null;
        if (problematicTransition != null) {
            String ref = stg.getSignalReference(problematicTransition);
            LogUtils.logWarning("Output '" + ref + "' becomes unexpectedly enabled");
            trace.add(stg.getNodeReference(problematicTransition));
            String comment = "Unexpected change of output '" + ref + "'";
            processedSolution = new MpsatSolution(trace, null, comment);
        } else if (!suspiciousSignals.isEmpty()) {
            String refs = String.join(", ", suspiciousSignals);
            LogUtils.logWarning("One of these outputs becomes unexpectedly enabled (via internal signals or dummies):\n" + refs);
            String comment = "Unexpected change of one of the outputs: " + refs;
            processedSolution = new MpsatSolution(trace, null, comment);
        }
        PetriUtils.setMarking(stg, marking);
        return processedSolution;
    }

    private HashMap<MpsatSolution, Enabledness> getSolutionToEnabledness(StgModel stg, List<MpsatSolution> solutions) {
        HashMap<MpsatSolution, Enabledness> result = new HashMap<>();
        for (MpsatSolution solution: solutions) {
            Trace trace = solution.getMainTrace();
            Enabledness enabledness = EnablednessUtils.getOutputEnablednessAfterTrace(stg, trace);
            result.put(solution, enabledness);
        }
        return result;
    }

}
