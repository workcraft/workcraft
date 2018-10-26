package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.graph.tools.Trace;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

class MpsatConformationOutputHandler extends MpsatReachabilityOutputHandler {

    private HashMap<MpsatSolution, Enabledness> solutionToEnabledness = null;

    MpsatConformationOutputHandler(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {

        super(we, exportOutput, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public List<MpsatSolution> processSolutions(WorkspaceEntry we, List<MpsatSolution> solutions) {
        List<MpsatSolution> result = new LinkedList<>();

        StgModel compStg = getMpsatOutput().getInputStg();
        HashMap<MpsatSolution, Enabledness> solutionToCompEnabledness = getSolutionToEnabledness(compStg, solutions);

        StgModel stg = getSrcStg(we);
        ComponentData data = getCompositionData(we);
        Map<String, String> substitutions = getSubstitutions(we);

        HashSet<String> visitedTraces = new HashSet<>();
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Unique projection(s) to '" + we.getTitle() + "':");
        }

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
        HashSet<String> suspiciousSignals = getEnabledSignals(stg, Signal.Type.OUTPUT);
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
            String ref = stg.getNodeReference(problematicTransition);
            LogUtils.logWarning("Output '" + ref + "' becomes unexpectedly enabled");
            trace.add(stg.getNodeReference(problematicTransition));
            String signalRef = stg.getSignalReference(problematicTransition);
            String comment = "Unexpected change of output '" + signalRef + "'";
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
        if (solutionToEnabledness == null) {
            solutionToEnabledness = new HashMap<>();
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
                Enabledness enabledness = getOutputEnablednessAfterTrace(stg, trace);
                solutionToEnabledness.put(solution, enabledness);
            }
        }
        return solutionToEnabledness;
    }

    private Enabledness getOutputEnablednessAfterTrace(StgModel stg, Trace trace) {
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        if (!PetriUtils.fireTrace(stg, trace)) {
            PetriUtils.setMarking(stg, marking);
            throw new RuntimeException("Cannot execute trace: " + trace.toText());
        }
        Signal.Type type = Signal.Type.OUTPUT;
        HashSet<String> enabled = getEnabledSignals(stg, type);
        HashSet<String> disabled = getDisabledSignals(stg, type);
        HashSet<String> unknown = new HashSet<>(stg.getSignalReferences(type));
        unknown.removeAll(enabled);
        unknown.removeAll(disabled);
        Enabledness enabledness = new Enabledness(enabled, disabled, unknown);
        PetriUtils.setMarking(stg, marking);
        return enabledness;
    }

    public static HashSet<String> getEnabledSignals(StgModel stg, Signal.Type type) {
        HashSet<String> result = new HashSet<>();
        for (SignalTransition signalTransition : stg.getSignalTransitions(type)) {
            if (stg.isEnabled(signalTransition)) {
                String signalRef = stg.getSignalReference(signalTransition);
                result.add(signalRef);
            }
        }
        return result;
    }

    public static HashSet<String> getDisabledSignals(StgModel stg, Signal.Type type) {
        HashSet<String> result = new HashSet<>(stg.getSignalReferences(type));
        for (SignalTransition signalTransition : stg.getSignalTransitions(type)) {
            String signalRef = stg.getSignalReference(signalTransition);
            if (stg.isEnabled(signalTransition) || hasPresetDummy(stg, signalTransition)) {
                result.remove(signalRef);
            }
        }
        return result;
    }

    private static boolean hasPresetDummy(StgModel stg, Transition transition) {
        for (MathNode predPlace : stg.getPreset(transition)) {
            for (MathNode predTransition : stg.getPreset(predPlace)) {
                if (predTransition instanceof DummyTransition) {
                    return true;
                }
            }
        }
        return false;
    }

}
