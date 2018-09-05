package org.workcraft.plugins.wtg.converter;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.NamedTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.wtg.State;
import org.workcraft.plugins.wtg.Waveform;
import org.workcraft.plugins.wtg.Wtg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WtgToStgConverter {
    private final Wtg srcModel;
    private final Stg dstModel;

    private final Map<State, StgPlace> stateToPlaceMap;
    private final Map<Event, NamedTransition> eventToTransitionMap;

    public WtgToStgConverter(Wtg srcModel, Stg dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;
        stateToPlaceMap = convertStates();
        eventToTransitionMap = convertWaveforms();
        convertConnections();
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
        DummyTransition entryTransition = dstModel.createDummyTransition(waveformName + "_entry", null);
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
        DummyTransition exitTransition = dstModel.createDummyTransition(waveformName + "_exit", null);
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
            String signalName = srcModel.getName(srcTransition.getSignal());
            org.workcraft.plugins.stg.SignalTransition dstTransition = dstModel.createSignalTransition(
                    signalName, convertWtgToStgDirection(srcTransition.getDirection()), null);
            dstTransition.setSignalType(convertWtgToStgType(srcTransition.getSignal().getType()));
            result.put(srcTransition, dstTransition);
        }
        return result;
    }

    private org.workcraft.plugins.stg.SignalTransition.Direction convertWtgToStgDirection(TransitionEvent.Direction direction) {
        switch (direction) {
        case RISE: return org.workcraft.plugins.stg.SignalTransition.Direction.PLUS;
        case FALL: return org.workcraft.plugins.stg.SignalTransition.Direction.MINUS;
        default: return org.workcraft.plugins.stg.SignalTransition.Direction.TOGGLE;
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

    public Wtg getSrcModel() {
        return srcModel;
    }

    public Stg getDstModel() {
        return dstModel;
    }

}
