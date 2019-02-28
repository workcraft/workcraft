package org.workcraft.plugins.mpsat.utils;

import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.tools.Trace;
import org.workcraft.plugins.mpsat.tasks.Enabledness;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;

import java.util.HashMap;
import java.util.HashSet;

public class EnablednessUtils {

    public static Enabledness getOutputEnablednessAfterTrace(StgModel stg, Trace trace) {
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
