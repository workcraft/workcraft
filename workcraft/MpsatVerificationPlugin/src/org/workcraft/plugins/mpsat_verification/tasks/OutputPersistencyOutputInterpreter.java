package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.projection.Enabledness;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Triple;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

class OutputPersistencyOutputInterpreter extends ReachabilityOutputInterpreter {

    OutputPersistencyOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(we, exportOutput, pcompOutput, mpsatOutput, interactive);
    }

    @Override
    public List<Solution> processSolutions(List<Solution> solutions) {
        List<Solution> result = new LinkedList<>();
        ComponentData data = getComponentData();
        StgModel stg = getStg();
        HashMap<Place, Integer> initialMarking = PetriUtils.getMarking(stg);
        for (Solution solution : solutions) {
            Trace trace = solution.getMainTrace();
            LogUtils.logMessage("Violation trace: " + trace);
            if (data != null) {
                trace = CompositionUtils.projectTrace(trace, data);
                LogUtils.logMessage("Projection trace: " + trace);
            }
            if (!PetriUtils.fireTrace(stg, trace)) {
                PetriUtils.setMarking(stg, initialMarking);
                throw new RuntimeException("Cannot execute trace: " + trace);
            }
            // Check if any local signal gets disabled by firing continuations
            Enabledness enabledness = CompositionUtils.getEnabledness(solution.getContinuations(), data);
            for (String enabledTransitionRef : enabledness.keySet()) {
                HashSet<String> nonpersistentLocalSignals = getNonpersistentLocalSignals(stg,
                        enabledness.get(enabledTransitionRef), enabledTransitionRef);

                if (!nonpersistentLocalSignals.isEmpty()) {
                    String comment = getMessageWithList("Non-persistent signal", nonpersistentLocalSignals);
                    String msg = getMessageWithList("Event '" + enabledTransitionRef + "' disables signal", nonpersistentLocalSignals);
                    LogUtils.logWarning(msg);
                    Trace processedTrace = new Trace(trace);
                    processedTrace.add(enabledTransitionRef);
                    Solution processedSolution = new Solution(processedTrace, null, comment);
                    result.add(processedSolution);
                }
            }
            PetriUtils.setMarking(stg, initialMarking);
        }
        return result;
    }

    private HashSet<String> getNonpersistentLocalSignals(StgModel stg, Trace continuationTrace, String enabledTransitionRef) {
        HashSet<String> result = new HashSet<>();
        continuationTrace.add(enabledTransitionRef);
        HashSet<String> enabledLocalSignals = getEnabledLocalSignals(stg);
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        if (PetriUtils.fireTrace(stg, continuationTrace)) {
            result.addAll(enabledLocalSignals);
            Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(enabledTransitionRef);
            if (r != null) {
                result.remove(r.getFirst());
            }
            HashSet<String> stillEnabledLocalSignals = getEnabledLocalSignals(stg);
            result.removeAll(stillEnabledLocalSignals);
        }
        PetriUtils.setMarking(stg, marking);
        return result;
    }

    private HashSet<String> getEnabledLocalSignals(StgModel stg) {
        HashSet<String> result = getEnabledSignals(stg);
        HashSet<String> localSignals = new HashSet<>();
        localSignals.addAll(stg.getSignalReferences(Signal.Type.OUTPUT));
        localSignals.addAll(stg.getSignalReferences(Signal.Type.INTERNAL));
        result.retainAll(localSignals);
        return result;
    }

    private HashSet<String> getEnabledSignals(StgModel stg) {
        HashSet<String> result = new HashSet<>();
        for (SignalTransition signalTransition : getEnabledSignalTransitions(stg)) {
            String signalRef = stg.getSignalReference(signalTransition);
            result.add(signalRef);
        }
        return result;
    }

    public static HashSet<SignalTransition> getEnabledSignalTransitions(StgModel stg) {
        HashSet<SignalTransition> result = new HashSet<>();
        for (Transition transition: stg.getTransitions()) {
            if (transition instanceof DummyTransition) {
                String ref = stg.getNodeReference(transition);
                throw new RuntimeException("Dummies are not supported in output persistency check: " + ref);
            }
            if ((transition instanceof SignalTransition) && stg.isEnabled(transition)) {
                SignalTransition signalTransition = (SignalTransition) transition;
                result.add(signalTransition);
            }
        }
        return result;
    }

    private String getMessageWithList(String message, Collection<String> refs) {
        if ((refs == null) || refs.isEmpty()) {
            return message;
        } else if (refs.size() == 1) {
            return message + " '" + refs.iterator().next() + "'";
        } else {
            return message + "s: " + String.join(", ", refs);
        }
    }

}
