package org.workcraft.plugins.fst.converters;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.Signal;
import org.workcraft.plugins.fst.SignalEvent.Direction;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.VisualSignalEvent;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.Hierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FstToStgConverter {
    private final VisualFst srcModel;
    private final VisualStg dstModel;

    private final Map<VisualState, VisualPlace> stateToPlaceMap;
    private final Map<VisualSignalEvent, VisualNamedTransition> eventToTransitionMap;
    private final Map<String, String> refToEventLabelMap;

    public FstToStgConverter(VisualFst srcModel, VisualStg dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;
        convertTitle();
        stateToPlaceMap = convertStates();
        eventToTransitionMap = convertEvents();
        refToEventLabelMap = cacheLabels();
        try {
            connectEvents();
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private void convertTitle() {
        dstModel.setTitle(srcModel.getTitle());
    }

    private Map<String, String> cacheLabels() {
        Map<String, String> result = new HashMap<>();
        for (Entry<VisualSignalEvent, VisualNamedTransition> entry: eventToTransitionMap.entrySet()) {
            VisualSignalEvent signalEvent = entry.getKey();
            VisualNamedTransition transition = entry.getValue();
            Signal signal = signalEvent.getReferencedConnection().getSymbol();
            String dstName = dstModel.getMathName(transition);
            String srcName = srcModel.getMathName(signal);
            if (signal.hasDirection()) {
                srcName += signalEvent.getReferencedConnection().getDirection();
            }
            result.put(dstName, srcName);
        }
        return result;
    }

    private Map<VisualState, VisualPlace> convertStates() {
        Map<VisualState, VisualPlace> result = new HashMap<>();
        for (VisualState state: srcModel.getVisualStates()) {
            String name = srcModel.getMathModel().getNodeReference(state.getReferencedComponent());
            VisualPlace place = dstModel.createVisualPlace(name);
            place.copyPosition(state);
            place.copyStyle(state);
            place.getReferencedComponent().setTokens(state.getReferencedComponent().isInitial() ? 1 : 0);
            place.setTokenColor(state.getForegroundColor());
            result.put(state, place);
        }
        return result;
    }

    private org.workcraft.plugins.stg.Signal.Type convertFstToStgType(Signal.Type type) {
        switch (type) {
        case INPUT: return org.workcraft.plugins.stg.Signal.Type.INPUT;
        case OUTPUT: return org.workcraft.plugins.stg.Signal.Type.OUTPUT;
        case INTERNAL: return org.workcraft.plugins.stg.Signal.Type.INTERNAL;
        case DUMMY: return null;
        }
        return null;
    }

    private org.workcraft.plugins.stg.SignalTransition.Direction convertFstToStgDirection(Direction direction) {
        switch (direction) {
        case PLUS: return org.workcraft.plugins.stg.SignalTransition.Direction.PLUS;
        case MINUS: return org.workcraft.plugins.stg.SignalTransition.Direction.MINUS;
        case TOGGLE: return org.workcraft.plugins.stg.SignalTransition.Direction.TOGGLE;
        }
        return null;
    }

    private Map<VisualSignalEvent, VisualNamedTransition> convertEvents() {
        Map<VisualSignalEvent, VisualNamedTransition> result = new HashMap<>();
        for (VisualSignalEvent signalEvent : srcModel.getVisualSignalEvents()) {
            VisualNamedTransition transition = null;
            Signal signal = signalEvent.getReferencedConnection().getSymbol();
            String name = srcModel.getMathName(signal);
            if (signal.hasDirection()) {
                Signal.Type srcType = signal.getType();
                org.workcraft.plugins.stg.Signal.Type dstType = convertFstToStgType(srcType);
                Direction srcDirection = signalEvent.getReferencedConnection().getDirection();
                org.workcraft.plugins.stg.SignalTransition.Direction dstDirection = convertFstToStgDirection(srcDirection);
                transition = dstModel.createVisualSignalTransition(name, dstType, dstDirection);
            } else {
                transition = dstModel.createVisualDummyTransition(name);
            }
            transition.setPosition(signalEvent.getCenter());
            transition.setForegroundColor(signalEvent.getColor());
            transition.setLabelColor(signalEvent.getLabelColor());
            result.put(signalEvent, transition);
        }
        return result;
    }

    private void connectEvents() throws InvalidConnectionException {
        for (VisualEvent event: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualEvent.class)) {
            VisualTransition transition = eventToTransitionMap.get(event);
            if (transition != null) {
                Node first = event.getFirst();
                if (first instanceof VisualState) {
                    VisualPlace inPlace = stateToPlaceMap.get(first);
                    if (inPlace != null) {
                        dstModel.connect(inPlace, transition);
                    }
                }
                Node second = event.getSecond();
                if (second instanceof VisualState) {
                    VisualPlace outPlace = stateToPlaceMap.get(second);
                    if (outPlace != null) {
                        dstModel.connect(transition, outPlace);
                    }
                }
            }
        }
    }

    public VisualFsm getSrcModel() {
        return srcModel;
    }

    public VisualStg getDstModel() {
        return dstModel;
    }

    public VisualPlace getRelatedPlace(VisualState state) {
        return stateToPlaceMap.get(state);
    }

    public VisualTransition getRelatedTransition(VisualEvent event) {
        return eventToTransitionMap.get(event);
    }

    public boolean isRelated(Node highLevelNode, Node node) {
        boolean result = false;
        if (highLevelNode instanceof VisualEvent) {
            VisualTransition relatedTransition = getRelatedTransition((VisualEvent) highLevelNode);
            if (relatedTransition != null) {
                result = (node == relatedTransition) || (node == relatedTransition.getReferencedComponent());
            }
        } else if (highLevelNode instanceof VisualState) {
            VisualPlace relatedPlace = getRelatedPlace((VisualState) highLevelNode);
            if (relatedPlace != null) {
                result = (node == relatedPlace) || (node == relatedPlace.getReferencedComponent());
            }
        }
        return result;
    }

    public String getEventLabel(String ref) {
        return refToEventLabelMap.get(ref);
    }

}
