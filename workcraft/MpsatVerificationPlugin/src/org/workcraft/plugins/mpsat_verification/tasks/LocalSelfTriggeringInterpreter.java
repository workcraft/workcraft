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

class LocalSelfTriggeringInterpreter extends ReachabilityOutputInterpreter {

    LocalSelfTriggeringInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
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
            // Check if any local signal is self-triggers by firing continuations
            Set<String> selfTriggeringLocalSignals = getSelfTriggeringLocalSignals(stg);
            if (!selfTriggeringLocalSignals.isEmpty()) {
                String comment = TextUtils.wrapMessageWithItems(
                        "Self-triggering signal", selfTriggeringLocalSignals);

                result.add(new Solution(trace, null, comment));
            }
            PetriUtils.setMarking(stg, initialMarking);
        }
        return result;
    }

    private HashSet<String> getSelfTriggeringLocalSignals(StgModel stg) {
        HashSet<String> result = new HashSet<>();
        HashSet<SignalTransition> enabledLocalSignalTransitions = getEnabledLocalSignalTransitions(stg);
        for (SignalTransition localSignalTransition : enabledLocalSignalTransitions) {
            String signalName = localSignalTransition.getSignalName();
            stg.fire(localSignalTransition);
            HashSet<SignalTransition> newEnabledLocalSignalTransitions = getEnabledLocalSignalTransitions(stg);
            newEnabledLocalSignalTransitions.removeAll(enabledLocalSignalTransitions);
            stg.unFire(localSignalTransition);
            // Check which newly enabled transitions are of the fired signal
            for (SignalTransition newEnabledLocalSignalTransition : newEnabledLocalSignalTransitions) {
                if (newEnabledLocalSignalTransition.getSignalName().equals(signalName)) {
                    result.add(signalName);
                }
            }
        }
        return result;
    }

    private HashSet<SignalTransition> getEnabledLocalSignalTransitions(StgModel stg) {
        HashSet<SignalTransition> result = new HashSet<>();
        for (Transition transition: stg.getTransitions()) {
            if (transition instanceof DummyTransition) {
                String ref = stg.getNodeReference(transition);
                throw new RuntimeException("Dummies are not supported in local self-triggering check: " + ref);
            }
            if ((transition instanceof SignalTransition) && stg.isEnabled(transition)) {
                SignalTransition signalTransition = (SignalTransition) transition;
                Signal.Type signalType = signalTransition.getSignalType();
                if ((signalType == Signal.Type.INTERNAL) || (signalType == Signal.Type.OUTPUT)) {
                    result.add(signalTransition);
                }
            }
        }
        return result;
    }

}
