package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.DiInterfaceDataPreserver;
import org.workcraft.plugins.mpsat_verification.presets.DiInterfaceParameters;
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
import org.workcraft.types.Pair;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

class DiInterfaceInterpreter extends ReachabilityOutputInterpreter {

    DiInterfaceInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(we, exportOutput, pcompOutput, mpsatOutput, interactive);
    }

    @Override
    public List<Solution> processSolutions(List<Solution> solutions) {
        List<Solution> result = new LinkedList<>();
        StgModel stg = getStg();
        DiInterfaceDataPreserver diInterfaceDataPreserver = new DiInterfaceDataPreserver(getWorkspaceEntry());
        DiInterfaceParameters diInterfaceParameters = diInterfaceDataPreserver.loadValidDataOnly();
        HashMap<Place, Integer> initialMarking = PetriUtils.getMarking(stg);
        for (Solution solution : solutions) {
            Trace trace = solution.getMainTrace();
            if (!PetriUtils.fireTrace(stg, trace)) {
                PetriUtils.setMarking(stg, initialMarking);
                throw new RuntimeException("Cannot execute trace: " + trace);
            }
            // Check if any input signal triggers any input signal
            Set<String> nonDiInputSignals = getNonDiInputSignals(stg, diInterfaceParameters);
            if (!nonDiInputSignals.isEmpty()) {
                String comment = TextUtils.wrapMessageWithItems(
                        "Interface is sensitive to input delays in the set", nonDiInputSignals);

                result.add(new Solution(trace, null, comment));
            }
            PetriUtils.setMarking(stg, initialMarking);
        }
        return result;
    }

    private Set<String> getNonDiInputSignals(StgModel stg, DiInterfaceParameters diInterfaceParameters) {
        Set<String> result = new HashSet<>();
        Set<String> selfTriggeringInputSignals = new HashSet<>();
        Set<Pair<String, String>> possibleInputSignalPairs = new HashSet<>();
        Set<SignalTransition> enabledInputSignalTransitions = getEnabledInputSignalTransitions(stg);
        for (SignalTransition enabledInputSignalTransition : enabledInputSignalTransitions) {
            String enabledInputSignal = stg.getSignalReference(enabledInputSignalTransition);
            stg.fire(enabledInputSignalTransition);
            HashSet<SignalTransition> nextEnabledInputSignalTransitions = getEnabledInputSignalTransitions(stg);
            stg.unFire(enabledInputSignalTransition);
            for (SignalTransition nextEnabledInputSignalTransition : nextEnabledInputSignalTransitions) {
                String nextEnabledInputSignal = stg.getSignalReference(nextEnabledInputSignalTransition);
                if (enabledInputSignal.equals(nextEnabledInputSignal)) {
                    selfTriggeringInputSignals.add(enabledInputSignal);
                } else {
                    possibleInputSignalPairs.add(new Pair<>(enabledInputSignal, nextEnabledInputSignal));
                }
            }
        }
        // Check for exceptions
        Set<String> addedSignals = new HashSet<>();
        for (Pair<String, String> possibleInputSignalPair : possibleInputSignalPairs) {
            String firstSignal = possibleInputSignalPair.getFirst();
            String secondSignal = possibleInputSignalPair.getSecond();
            Pair<String, String> reverseInputSignalPair = Pair.of(secondSignal, firstSignal);
            if (!possibleInputSignalPairs.contains(reverseInputSignalPair)) {
                Set<String> violationInputSignals = new HashSet<>();
                violationInputSignals.add(firstSignal);
                violationInputSignals.add(secondSignal);
                if (!diInterfaceParameters.isException(violationInputSignals)) {
                    result.add("{" + String.join(", ", SortUtils.getSortedNatural(violationInputSignals)) + "}");
                    addedSignals.addAll(violationInputSignals);
                }
            }
        }
        for (String selfTriggeringInputSignal : selfTriggeringInputSignals) {
            if (!addedSignals.contains(selfTriggeringInputSignal)
                    && !diInterfaceParameters.isException(Set.of(selfTriggeringInputSignal))) {

                result.add("{" + selfTriggeringInputSignal + "}");
            }
        }
        return result;
    }

    private HashSet<SignalTransition> getEnabledInputSignalTransitions(StgModel stg) {
        HashSet<SignalTransition> result = new HashSet<>();
        for (Transition transition: stg.getTransitions()) {
            if (transition instanceof DummyTransition) {
                String ref = stg.getNodeReference(transition);
                throw new RuntimeException("Dummies are not supported in delay insensitive interface check: " + ref);
            }
            if ((transition instanceof SignalTransition signalTransition) && stg.isEnabled(transition)) {
                if (signalTransition.getSignalType() == Signal.Type.INPUT) {
                    result.add(signalTransition);
                }
            }
        }
        return result;
    }

}
