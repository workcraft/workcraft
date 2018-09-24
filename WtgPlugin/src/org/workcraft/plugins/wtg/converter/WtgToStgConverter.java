package org.workcraft.plugins.wtg.converter;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.wtg.*;
import org.workcraft.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.workcraft.plugins.wtg.utils.WtgUtils.getInitialSignalStates;
import static org.workcraft.plugins.wtg.utils.WtgUtils.getUnstableSignalNames;

public class WtgToStgConverter {

    private final Wtg srcModel;
    private final Stg dstModel;

    private final Map<State, StgPlace> stateToPlaceMap;
    private final Map<String, UnstableSignalStg> unstableSignalToStgMap;
    private final Map<Waveform, Pair<NamedTransition, NamedTransition>> waveformToEntryExitMap;
    private final Map<Event, NamedTransition> eventToTransitionMap;

    public WtgToStgConverter(Wtg srcModel, Stg dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;
        stateToPlaceMap = convertStates();
        unstableSignalToStgMap = createSignalStatePlaces();
        waveformToEntryExitMap = convertWaveforms();
        eventToTransitionMap = convertEvents();
        convertConnections();
        convertGuards();
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

    private Map<String, UnstableSignalStg> createSignalStatePlaces() {
        Map<String, UnstableSignalStg> result = new HashMap<>();
        Map<String, Signal.State> initialSignalStates = getInitialSignalStates(srcModel);
        for (String signalName: getUnstableSignalNames(srcModel)) {
            // Only input signals can be unstable
            org.workcraft.plugins.stg.Signal.Type signalType = org.workcraft.plugins.stg.Signal.Type.INPUT;

            StgPlace lowPlace = dstModel.createPlace(getLowStateName(signalName), null);
            StgPlace highPlace = dstModel.createPlace(getHighStateName(signalName), null);
            StgPlace unstablePlace = dstModel.createPlace(getUnstableStateName(signalName), null);
            StgPlace stablePlace = dstModel.createPlace(getStableStateName(signalName), null);

            Signal.State initialSignalState = initialSignalStates.get(signalName);
            if (initialSignalState == Signal.State.UNSTABLE) {
                stablePlace.setTokens(0);
                unstablePlace.setTokens(1);
            } else {
                stablePlace.setTokens(1);
                unstablePlace.setTokens(0);
            }
            if (initialSignalState == Signal.State.HIGH) {
                highPlace.setTokens(1);
                lowPlace.setTokens(0);
            } else {
                //If the initial Signal State is unstable or stable, we default to 0. This is an arbitrary decision
                highPlace.setTokens(0);
                lowPlace.setTokens(1);
            }

            SignalTransition riseTransition = dstModel.createSignalTransition(signalName,
                    SignalTransition.Direction.PLUS, null);
            riseTransition.setSignalType(signalType);

            SignalTransition fallTransition = dstModel.createSignalTransition(signalName,
                    SignalTransition.Direction.MINUS, null);
            fallTransition.setSignalType(signalType);

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
            result.put(signalName, signalStg);
        }
        return result;
    }

    private Map<Waveform, Pair<NamedTransition, NamedTransition>> convertWaveforms() {
        Map<Waveform, Pair<NamedTransition, NamedTransition>> result = new HashMap<>();
        for (Waveform waveform : srcModel.getWaveforms()) {
            result.put(waveform, convertWaveform(waveform));
        }
        return result;
    }

    private Pair<NamedTransition, NamedTransition> convertWaveform(Waveform waveform) {
        Set<Node> preset = srcModel.getPreset(waveform);
        Set<Node> postset = srcModel.getPostset(waveform);
        if ((preset.size() != 1) || (postset.size() != 1)) {
            String waveformName = srcModel.getName(waveform);
            throw new FormatException("Incorrect preset and/or postset of waveform '" + waveformName + "'");
        }
        String waveformName = srcModel.getName(waveform);
        // Waveform entry
        DummyTransition entryTransition = null;
        Node entryNode = preset.iterator().next();
        if (entryNode instanceof State) {
            State entryState = (State) entryNode;
            StgPlace entryPlace = stateToPlaceMap.get(entryState);
            entryTransition = dstModel.createDummyTransition(getEntryEventName(waveformName), null);
            try {
                dstModel.connect(entryPlace, entryTransition);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        // Waveform exit
        DummyTransition exitTransition = null;
        Node exitNode = postset.iterator().next();
        if (exitNode instanceof State) {
            State exitState = (State) exitNode;
            StgPlace exitPlace = stateToPlaceMap.get(exitState);
            exitTransition = dstModel.createDummyTransition(getExitEventName(waveformName), null);
            try {
                dstModel.connect(exitTransition, exitPlace);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        return Pair.of(entryTransition, exitTransition);
    }

    private Map<Event, NamedTransition> convertEvents() {
        Map<Event, NamedTransition> result = new HashMap<>();
        for (Waveform waveform : srcModel.getWaveforms()) {
            result.putAll(convertEvents(waveform));
        }
        return result;
    }

    private Map<Event, NamedTransition> convertEvents(Waveform waveform) {
        Map<Event, NamedTransition> result = new HashMap<>();
        // Entry events
        result.putAll(convertEntryEvents(waveform));
        // Exit events
        result.putAll(convertExitEvents(waveform));
        // Transition events
        result.putAll(convertTransitionEvents(waveform));
        return result;
    }

    private Map<Event, NamedTransition> convertEntryEvents(Waveform waveform) {
        Map<Event, NamedTransition> result = new HashMap<>();
        NamedTransition entryTransition = waveformToEntryExitMap.get(waveform).getFirst();
        for (EntryEvent entryEvent : srcModel.getEntries(waveform)) {
            result.put(entryEvent, entryTransition);
        }
        return result;
    }

    private Map<Event, NamedTransition> convertExitEvents(Waveform waveform) {
        Map<Event, NamedTransition> result = new HashMap<>();
        NamedTransition exitTransition = waveformToEntryExitMap.get(waveform).getSecond();
        for (ExitEvent signalExit : srcModel.getExits(waveform)) {
            result.put(signalExit, exitTransition);
        }
        return result;
    }

    private Map<Event, NamedTransition> convertTransitionEvents(Waveform waveform) {
        Map<Event, NamedTransition> result = new HashMap<>();
        for (TransitionEvent srcTransition : srcModel.getTransitions(waveform)) {
            TransitionEvent.Direction direction = srcTransition.getDirection();
            if (direction == TransitionEvent.Direction.DESTABILISE) {
                result.put(srcTransition, convertDestabiliseTransitionEvent(srcTransition));
            } else if (srcModel.getPreviousState(srcTransition) == Signal.State.UNSTABLE) {
                result.put(srcTransition, convertStabiliseTransitionEvent(srcTransition));
            } else {
                Signal signal = srcTransition.getSignal();
                String signalName = srcModel.getName(signal);
                SignalTransition dstTransition = dstModel.createSignalTransition(signalName,
                        convertWtgToStgDirection(direction), null);
                dstTransition.setSignalType(convertWtgToStgType(signal.getType()));
                result.put(srcTransition, dstTransition);

                if (unstableSignalToStgMap.containsKey(signalName)) {
                    convertUnstableSignalStableTransitionEvent(srcTransition, dstTransition);
                }
            }
        }
        return result;
    }

    private DummyTransition convertDestabiliseTransitionEvent(TransitionEvent srcTransition) {
        Signal signal = srcTransition.getSignal();
        String signalName = srcModel.getName(signal);
        String dummyName = getDestabiliseEventName(signalName);
        DummyTransition dstTransition = dstModel.createDummyTransition(dummyName, null);
        UnstableSignalStg signalStg = unstableSignalToStgMap.get(signalName);
        if (signalStg != null) {
            try {
                dstModel.connect(signalStg.stablePlace, dstTransition);
                dstModel.connect(dstTransition, signalStg.unstablePlace);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        return dstTransition;
    }

    private DummyTransition convertStabiliseTransitionEvent(TransitionEvent srcTransition) {
        Signal signal = srcTransition.getSignal();
        String signalName = srcModel.getName(signal);
        String dummyName = getStabiliseEventName(signalName);
        DummyTransition dstTransition = dstModel.createDummyTransition(dummyName, null);
        UnstableSignalStg signalStg = unstableSignalToStgMap.get(signalName);
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
        return dstTransition;
    }

    private void convertUnstableSignalStableTransitionEvent(TransitionEvent srcTransition, SignalTransition dstTransition) {
        UnstableSignalStg signalStg = unstableSignalToStgMap.get(srcModel.getName(srcTransition.getSignal()));
        if (srcTransition.getDirection() == TransitionEvent.Direction.RISE) {
            // Signal goes from stable zero to stable one
            try {
                dstModel.connect(signalStg.lowPlace, dstTransition);
                dstModel.connect(dstTransition, signalStg.highPlace);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        } else if (srcTransition.getDirection() == TransitionEvent.Direction.FALL) {
            // Signal goes from stable one to stable zero
            try {
                dstModel.connect(signalStg.highPlace, dstTransition);
                dstModel.connect(dstTransition, signalStg.lowPlace);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
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

    private void convertGuards() {
        for (Waveform waveform : srcModel.getWaveforms()) {
            convertGuard(waveform);
        }
    }

    private void convertGuard(Waveform waveform) {
        NamedTransition entryTransition = waveformToEntryExitMap.get(waveform).getFirst();
        Guard guard = waveform.getGuard();
        for (String signalName : guard.keySet()) {
            UnstableSignalStg signalStg = unstableSignalToStgMap.get(signalName);
            if (signalStg == null) {
                throw new RuntimeException("Cannot find signal '" + signalName + "' used in the guard.");
            }
            StgPlace place = guard.get(signalName) ? signalStg.highPlace : signalStg.lowPlace;
            try {
                // Read-arc from STABLE place
                dstModel.connect(signalStg.stablePlace, entryTransition);
                dstModel.connect(entryTransition, signalStg.stablePlace);
                // Read-arc from HIGH or LOW place
                dstModel.connect(place, entryTransition);
                dstModel.connect(entryTransition, place);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String getLowStateName(String signalName) {
        return signalName + WtgSettings.getLowStateSuffix();
    }

    public static String getHighStateName(String signalName) {
        return signalName + WtgSettings.getHighStateSuffix();
    }

    public static String getStableStateName(String signalName) {
        return signalName + WtgSettings.getStableStateSuffix();
    }

    public static String getUnstableStateName(String signalName) {
        return signalName + WtgSettings.getUnstableStateSuffix();
    }

    public static String getStabiliseEventName(String signalName) {
        return signalName + WtgSettings.getStabiliseEventSuffix();
    }

    public static String getDestabiliseEventName(String signalName) {
        return signalName + WtgSettings.getDestabiliseEventSuffix();
    }

    public static String getEntryEventName(String signalName) {
        return signalName + WtgSettings.getEntryEventSuffix();
    }

    public static String getExitEventName(String signalName) {
        return signalName + WtgSettings.getExitEventSuffix();
    }

    public Wtg getSrcModel() {
        return srcModel;
    }

    public Stg getDstModel() {
        return dstModel;
    }

    public boolean isRelated(Node highLevelNode, Node node) {
        boolean result = false;
        if (highLevelNode instanceof Event) {
            NamedTransition relatedTransition = getRelatedTransition((Event) highLevelNode);
            result = node == relatedTransition;
        } else if (highLevelNode instanceof State) {
            StgPlace relatedPlace = getRelatedPlace((State) highLevelNode);
            result = node == relatedPlace;
        }
        return result;
    }

    public StgPlace getRelatedPlace(State state) {
        return stateToPlaceMap.get(state);
    }

    public NamedTransition getRelatedTransition(Event event) {
        return eventToTransitionMap.get(event);
    }

}
