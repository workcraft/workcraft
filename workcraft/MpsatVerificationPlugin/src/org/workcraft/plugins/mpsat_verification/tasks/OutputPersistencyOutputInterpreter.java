package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

class OutputPersistencyOutputInterpreter extends ReachabilityOutputInterpreter {

    OutputPersistencyOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(we, exportOutput, pcompOutput, mpsatOutput, interactive);
    }

    @Override
    public List<Solution> processSolutions(WorkspaceEntry we, List<Solution> solutions) {
        List<Solution> result = new LinkedList<>();

        Map<String, String> substitutions = getSubstitutions(we);
        ComponentData data = getCompositionData(we);
        StgModel stg = getSrcStg(we);
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);

        for (Solution solution: solutions) {
            if ((solution == null) || (stg == null)) {
                result.add(solution);
            }
            Trace trace = solution.getMainTrace();
            LogUtils.logMessage("Violation trace: " + trace.toString());
            if (data != null) {
                trace = getProjectedTrace(trace, data, substitutions);
                LogUtils.logMessage("Projection trace: " + trace.toString());
            }
            if (!PetriUtils.fireTrace(stg, trace)) {
                PetriUtils.setMarking(stg, marking);
                throw new RuntimeException("Cannot execute trace: " + trace.toString());
            }
            // Check if any local signal gets disabled by firing other signal event
            HashSet<String> enabledLocalSignals = getEnabledLocalSignals(stg);
            for (SignalTransition transition : getEnabledSignalTransitions(stg)) {
                stg.fire(transition);
                HashSet<String> nonpersistentLocalSignals = new HashSet<>(enabledLocalSignals);
                nonpersistentLocalSignals.remove(transition.getSignalName());
                nonpersistentLocalSignals.removeAll(getEnabledLocalSignals(stg));
                if (!nonpersistentLocalSignals.isEmpty()) {
                    String comment = getMessageWithList("Non-persistent signal", nonpersistentLocalSignals);
                    String transitionRef = stg.getNodeReference(transition);
                    String msg = getMessageWithList("Event '" + transitionRef + "' disables signal", nonpersistentLocalSignals);
                    LogUtils.logWarning(msg);
                    Trace processedTrace = new Trace(trace);
                    processedTrace.add(transitionRef);
                    Solution processedSolution = new Solution(processedTrace, null, comment);
                    result.add(processedSolution);
                }
                stg.unFire(transition);
            }
            PetriUtils.setMarking(stg, marking);
        }
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
