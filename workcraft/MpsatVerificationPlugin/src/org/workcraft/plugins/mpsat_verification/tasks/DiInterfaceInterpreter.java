package org.workcraft.plugins.mpsat_verification.tasks;

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
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.LogUtils;
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
            // Check if any input signal triggers any input signal
            Set<String> nonDiInputSignals = getNonDiInputSignals(stg);
            if (!nonDiInputSignals.isEmpty()) {
                String comment = TextUtils.wrapMessageWithItems(
                        "Interface is sensitive to input delays in the set", nonDiInputSignals);

                result.add(new Solution(trace, null, comment));
            }
            PetriUtils.setMarking(stg, initialMarking);
        }
        return result;
    }

    private HashSet<String> getNonDiInputSignals(StgModel stg) {
        HashSet<String> result = new HashSet<>();
        HashSet<SignalTransition> enabledInputSignalTransitions = getEnabledInputSignalTransitions(stg);
        for (SignalTransition inputSignalTransition : enabledInputSignalTransitions) {
            String signalName = inputSignalTransition.getSignalName();
            stg.fire(inputSignalTransition);
            HashSet<SignalTransition> newEnabledInputSignalTransitions = getEnabledInputSignalTransitions(stg);
            newEnabledInputSignalTransitions.removeAll(enabledInputSignalTransitions);
            stg.unFire(inputSignalTransition);
            // Check which newly enabled transitions are of input signal
            for (SignalTransition newEnabledLocalSignalTransition : newEnabledInputSignalTransitions) {
                if (newEnabledLocalSignalTransition.getSignalType() == Signal.Type.INPUT) {
                    Set<String> signalNames = new HashSet<>();
                    signalNames.add(signalName);
                    signalNames.add(newEnabledLocalSignalTransition.getSignalName());
                    result.add("{" + String.join(", ", signalNames) + "}");
                }
            }
        }
        return result;
    }

    private HashSet<SignalTransition> getEnabledInputSignalTransitions(StgModel stg) {
        HashSet<SignalTransition> result = new HashSet<>();
        for (Transition transition: stg.getTransitions()) {
            if (transition instanceof DummyTransition) {
                String ref = stg.getNodeReference(transition);
                throw new RuntimeException("Dummies are not supported in local self-triggering check: " + ref);
            }
            if ((transition instanceof SignalTransition) && stg.isEnabled(transition)) {
                SignalTransition signalTransition = (SignalTransition) transition;
                if (signalTransition.getSignalType() == Signal.Type.INPUT) {
                    result.add(signalTransition);
                }
            }
        }
        return result;
    }

}
