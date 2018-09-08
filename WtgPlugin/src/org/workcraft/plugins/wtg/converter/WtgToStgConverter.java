package org.workcraft.plugins.wtg.converter;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.wtg.State;
import org.workcraft.plugins.wtg.Waveform;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.plugins.wtg.WtgSettings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WtgToStgConverter {

    private final Wtg srcModel;
    private final Stg dstModel;

    private final Map<State, StgPlace> stateToPlaceMap;
    private final Map<Event, NamedTransition> eventToTransitionMap;
    private final Map<Signal, UnstableSignalStg> unstableSignalToStgMap;

    public WtgToStgConverter(Wtg srcModel, Stg dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;
        stateToPlaceMap = convertStates();
        unstableSignalToStgMap = createSignalStatePlaces();
        eventToTransitionMap = convertWaveforms();
        convertConnections();
    }

    private Map<Signal, UnstableSignalStg> createSignalStatePlaces() {
        Map<Signal, UnstableSignalStg> result = new HashMap<>();
        for (Signal signal: getUnstableSignals()) {
            String signalName =  srcModel.getName(signal);
            StgPlace lowPlace = dstModel.createPlace(getLowStateName(signalName), null);
            StgPlace highPlace = dstModel.createPlace(getHighStateName(signalName), null);
            StgPlace unstablePlace = dstModel.createPlace(getUnstableStateName(signalName), null);
            StgPlace stablePlace = dstModel.createPlace(getStableStateName(signalName), null);

            // FIXME: We (provisionally) assume that unstable signals are initially in STABLE-LOW state
            stablePlace.setTokens(1);
            unstablePlace.setTokens(0);
            lowPlace.setTokens(1);
            highPlace.setTokens(0);

            SignalTransition riseTransition = dstModel.createSignalTransition(signalName,
                    SignalTransition.Direction.PLUS, null);

            SignalTransition fallTransition = dstModel.createSignalTransition(signalName,
                    SignalTransition.Direction.MINUS, null);

            UnstableSignalStg signalStg = new UnstableSignalStg(lowPlace, highPlace,
                    fallTransition, riseTransition, stablePlace, unstablePlace);

            try {
                dstModel.connect(lowPlace, riseTransition);
                dstModel.connect(riseTransition, highPlace);
                dstModel.connect(highPlace, fallTransition);
                dstModel.connect(fallTransition, lowPlace);
                // Read-arc to fall transition
                dstModel.connect(fallTransition, unstablePlace);
                dstModel.connect(unstablePlace, fallTransition);
                // Read-arc to rise transition
                dstModel.connect(riseTransition, unstablePlace);
                dstModel.connect(unstablePlace, riseTransition);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
            result.put(signal, signalStg);
        }
        return result;
    }

    private Map<State, StgPlace> convertStates() {
        Map<State, StgPlace> result = new HashMap<>();
        for (State state: srcModel.getStates()) {
            String name = srcModel.getName(state);
            StgPlace place = dstModel.createPlace(name, null);
            place.setTokens(state.isInitial() ? 1 : 0);
            result.put(state, place);
        }
        return result;
    }

    private Map<Event, NamedTransition> convertWaveforms() {
        Map<Event, NamedTransition> result = new HashMap<>();
        for (Waveform waveform : srcModel.getWaveforms()) {
            result.putAll(convertWaveform(waveform));
        }
        return result;
    }

    private Map<Event, NamedTransition> convertWaveform(Waveform waveform) {
        Set<Node> preset = srcModel.getPreset(waveform);
        Set<Node> postset = srcModel.getPostset(waveform);
        if ((preset.size() != 1) || (postset.size() != 1)) {
            String waveformName = srcModel.getName(waveform);
            throw new FormatException("Incorrect preset and/or postset of waveform '" + waveformName + "'");
        }
        Map<Event, NamedTransition> result = new HashMap<>();
        // Entry events
        Node entryNode = preset.iterator().next();
        if (entryNode instanceof State) {
            State entryState = (State) entryNode;
            result.putAll(convertWaveformEntry(waveform, entryState));
        }
        // Exit events
        Node exitNode = postset.iterator().next();
        if (exitNode instanceof State) {
            State exitState = (State) exitNode;
            result.putAll(convertWaveformExit(waveform, exitState));
        }
        // Transition events
        result.putAll(convertWaveformTransitions(waveform));
        return result;
    }

    private Map<Event, NamedTransition> convertWaveformEntry(Waveform waveform, State entryState) {
        Map<Event, NamedTransition> result = new HashMap<>();
        StgPlace entryPlace = stateToPlaceMap.get(entryState);
        String waveformName = srcModel.getName(waveform);
        DummyTransition entryTransition = dstModel.createDummyTransition(getEntryEventName(waveformName), null);
        try {
            dstModel.connect(entryPlace, entryTransition);
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
        for (EntryEvent entryEvent : srcModel.getEntries(waveform)) {
            result.put(entryEvent, entryTransition);
        }
        return result;
    }

    private Map<Event, NamedTransition> convertWaveformExit(Waveform waveform, State exitState) {
        Map<Event, NamedTransition> result = new HashMap<>();
        StgPlace exitPlace = stateToPlaceMap.get(exitState);
        String waveformName = srcModel.getName(waveform);
        DummyTransition exitTransition = dstModel.createDummyTransition(getExitEventName(waveformName), null);
        try {
            dstModel.connect(exitTransition, exitPlace);
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
        for (ExitEvent signalExit : srcModel.getExits(waveform)) {
            result.put(signalExit, exitTransition);
        }
        return result;
    }

    private Map<Event, NamedTransition> convertWaveformTransitions(Waveform waveform) {
        Map<Event, NamedTransition> result = new HashMap<>();
        for (TransitionEvent srcTransition : srcModel.getTransitions(waveform)) {
            Signal signal = srcTransition.getSignal();
            String signalName = srcModel.getName(signal);

            TransitionEvent.Direction direction = srcTransition.getDirection();
            if (direction == TransitionEvent.Direction.DESTABILISE) {
                // Destabilisation
                String dummyName = getDestabiliseEventName(signalName);
                DummyTransition dstTransition = dstModel.createDummyTransition(dummyName, null);
                UnstableSignalStg signalStg = unstableSignalToStgMap.get(signal);
                if (signalStg != null) {
                    try {
                        dstModel.connect(signalStg.stablePlace, dstTransition);
                        dstModel.connect(dstTransition, signalStg.unstablePlace);
                    } catch (InvalidConnectionException e) {
                        throw new RuntimeException(e);
                    }
                }
                result.put(srcTransition, dstTransition);
            } else if (srcModel.getPreviousState(srcTransition) == Signal.State.UNSTABLE) {
                // Stabilisation
                String dummyName = getStabiliseEventName(signalName);
                DummyTransition dstTransition = dstModel.createDummyTransition(dummyName, null);
                UnstableSignalStg signalStg = unstableSignalToStgMap.get(signal);
                if (signalStg != null) {
                    try {
                        dstModel.connect(signalStg.unstablePlace, dstTransition);
                        dstModel.connect(dstTransition, signalStg.stablePlace);
                    } catch (InvalidConnectionException e) {
                        throw new RuntimeException(e);
                    }
                    if (srcTransition.getDirection() == TransitionEvent.Direction.RISE) {
                        // Signal is stable high
                        try {
                            dstModel.connect(signalStg.highPlace, dstTransition);
                            dstModel.connect(dstTransition, signalStg.highPlace);
                        } catch (InvalidConnectionException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (srcTransition.getDirection() == TransitionEvent.Direction.FALL) {
                        // Signal is stable low
                        try {
                            dstModel.connect(signalStg.lowPlace, dstTransition);
                            dstModel.connect(dstTransition, signalStg.lowPlace);
                        } catch (InvalidConnectionException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
                result.put(srcTransition, dstTransition);
            } else {
                // Normal transition
                SignalTransition dstTransition = dstModel.createSignalTransition(signalName,
                        convertWtgToStgDirection(direction), null);
                dstTransition.setSignalType(convertWtgToStgType(signal.getType()));
                result.put(srcTransition, dstTransition);
            }
        }
        return result;
    }

    private Set<Signal> getUnstableSignals() {
        Set<Signal> result = new HashSet<>();
        for (Waveform waveform : srcModel.getWaveforms()) {
            for (TransitionEvent srcTransition : srcModel.getTransitions(waveform)) {
                TransitionEvent.Direction direction = srcTransition.getDirection();
                if ((direction == TransitionEvent.Direction.DESTABILISE) || (direction == TransitionEvent.Direction.STABILISE)) {
                    result.add(srcTransition.getSignal());
                }
            }
        }
        return result;
    }

    private SignalTransition.Direction convertWtgToStgDirection(TransitionEvent.Direction direction) {
        switch (direction) {
        case RISE: return SignalTransition.Direction.PLUS;
        case FALL: return SignalTransition.Direction.MINUS;
        default: return SignalTransition.Direction.TOGGLE;
        }
    }

    private org.workcraft.plugins.stg.Signal.Type convertWtgToStgType(Signal.Type type) {
        switch (type) {
        case INPUT: return org.workcraft.plugins.stg.Signal.Type.INPUT;
        case OUTPUT: return org.workcraft.plugins.stg.Signal.Type.OUTPUT;
        case INTERNAL: return org.workcraft.plugins.stg.Signal.Type.INTERNAL;
        default: return null;
        }
    }

    private void convertConnections() {
        for (Waveform waveform : srcModel.getWaveforms()) {
            convertConnections(waveform);
        }
    }

    private void convertConnections(Waveform waveform) {
        for (Event event : srcModel.getEvents(waveform)) {
            NamedTransition fromTransition = eventToTransitionMap.get(event);
            for (Node node: srcModel.getPostset(event)) {
                NamedTransition toTransition = eventToTransitionMap.get(node);
                if (isRedundantConnection(event, node)) continue;
                try {
                    dstModel.connect(fromTransition, toTransition);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private boolean isRedundantConnection(Node fromNode, Node toNode) {
        if (fromNode instanceof EntryEvent) {
            Set<Node> preset = srcModel.getPreset(toNode);
            if ((preset.size() > 1) && preset.contains(fromNode)) {
                return true;
            }
        }
        if (toNode instanceof ExitEvent) {
            Set<Node> postset = srcModel.getPostset(fromNode);
            if ((postset.size() > 1) && postset.contains(toNode)) {
                return true;
            }
        }
        return false;
    }

    private static String getLowStateName(String signalName) {
        return signalName + WtgSettings.getLowStateSuffix();
    }

    private static String getHighStateName(String signalName) {
        return signalName + WtgSettings.getHighStateSuffix();
    }

    private static String getStableStateName(String signalName) {
        return signalName + WtgSettings.getStableStateSuffix();
    }

    private static String getUnstableStateName(String signalName) {
        return signalName + WtgSettings.getUnstableStateSuffix();
    }

    private static String getStabiliseEventName(String signalName) {
        return signalName + WtgSettings.getStabiliseEventSuffix();
    }

    private static String getDestabiliseEventName(String signalName) {
        return signalName + WtgSettings.getDestabiliseEventSuffix();
    }

    private static String getEntryEventName(String signalName) {
        return signalName + WtgSettings.getEntryEventSuffix();
    }

    private static String getExitEventName(String signalName) {
        return signalName + WtgSettings.getExitEventSuffix();
    }

    public Wtg getSrcModel() {
        return srcModel;
    }

    public Stg getDstModel() {
        return dstModel;
    }

}
