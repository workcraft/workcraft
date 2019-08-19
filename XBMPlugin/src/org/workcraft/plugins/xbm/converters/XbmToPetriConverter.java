package org.workcraft.plugins.xbm.converters;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.fsm.*;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.xbm.*;
import org.workcraft.utils.Hierarchy;

import java.util.HashMap;
import java.util.Map;

public class XbmToPetriConverter {

    private final VisualXbm srcModel;
    private final VisualPetri dstModel;

    private final Map<VisualXbmState, VisualPlace> stateToPlaceMap;
    private final Map<VisualBurstEvent, VisualTransition> eventToTransitionMap;
    private final Map<String, String> refToBurstEventLabelMap;

    public XbmToPetriConverter(VisualXbm srcModel, VisualPetri dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;
        stateToPlaceMap = convertStates();
        eventToTransitionMap = convertEvents();
        refToBurstEventLabelMap = cacheLabels();
        try {
            connectEvents();
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> cacheLabels() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<VisualBurstEvent, VisualTransition> entry: eventToTransitionMap.entrySet()) {
            VisualBurstEvent event = entry.getKey();
            VisualTransition transition = entry.getValue();
            String dstName = dstModel.getMathName(transition);
            String srcName = (event == null) ? "" : event.getReferencedBurstEvent().getAsString();
            result.put(dstName, srcName);
        }
        return result;
    }

    private Map<VisualXbmState, VisualPlace> convertStates() {
        Map<VisualXbmState, VisualPlace> result = new HashMap<>();
        for (VisualXbmState state: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualXbmState.class)) {
            String name = srcModel.getMathModel().getNodeReference(state.getReferencedState());
            VisualPlace place = dstModel.createPlace(name, null);
            place.copyPosition(state);
            place.copyStyle(state);
            place.getReferencedPlace().setTokens(state.getReferencedState().isInitial() ? 1 : 0);
            place.setTokenColor(state.getForegroundColor());
            result.put(state, place);
        }
        return result;
    }

    private Map<VisualBurstEvent, VisualTransition> convertEvents() {
        Map<VisualBurstEvent, VisualTransition> result = new HashMap<>();
        HierarchyReferenceManager refManager = dstModel.getPetriNet().getReferenceManager();
        for (VisualBurstEvent event : Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualBurstEvent.class)) {
            Burst burst = event.getReferencedBurstEvent().getBurst();
            String symbolName = burst.getAsString();
            String name = refManager.getName(event);
            VisualTransition transition = dstModel.createTransition(name, null);
            transition.setPosition(event.getCenter());
            transition.setForegroundColor(event.getColor());
            if (burst != null) {
                transition.setLabel(symbolName);
            }
            transition.setLabelColor(event.getLabelColor());
            result.put(event, transition);
        }
        return result;
    }

    private void connectEvents() throws InvalidConnectionException {
        for (VisualBurstEvent event: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualBurstEvent.class)) {
            VisualTransition transition = eventToTransitionMap.get(event);
            if (transition != null) {
                Node first = event.getFirst();
                if (first instanceof VisualXbmState) {
                    VisualPlace inPlace = stateToPlaceMap.get(first);
                    if (inPlace != null) {
                        dstModel.connect(inPlace, transition);
                    }
                }
                Node second = event.getSecond();
                if (second instanceof VisualXbmState) {
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

    public VisualPetri getDstModel() {
        return dstModel;
    }

    public VisualPlace getRelatedPlace(VisualXbmState state) {
        return stateToPlaceMap.get(state);
    }

    public VisualTransition getRelatedTransition(VisualEvent event) {
        return eventToTransitionMap.get(event);
    }

    public VisualXbmState getRelatedState(VisualPlace place) {
        for (Map.Entry<VisualXbmState, VisualPlace> entry: stateToPlaceMap.entrySet()) {
            if (entry.getValue() == place) {
                return entry.getKey();
            }
        }
        return null;
    }

    public VisualBurstEvent getRelatedEvent(VisualTransition transition) {
        for (Map.Entry<VisualBurstEvent, VisualTransition> entry: eventToTransitionMap.entrySet()) {
            if (entry.getValue() == transition) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isRelated(Node highLevelNode, Node node) {
        boolean result = false;
        if (highLevelNode instanceof VisualEvent) {
            VisualTransition relatedTransition = getRelatedTransition((VisualEvent) highLevelNode);
            if (relatedTransition != null) {
                result = (node == relatedTransition) || (node == relatedTransition.getReferencedComponent());
            }
        } else if (highLevelNode instanceof VisualXbmState) {
            VisualPlace relatedPlace = getRelatedPlace((VisualXbmState) highLevelNode);
            if (relatedPlace != null) {
                result = (node == relatedPlace) || (node == relatedPlace.getReferencedComponent());
            }
        }
        return result;
    }

    public String getSymbol(String ref) {
        return refToBurstEventLabelMap.get(ref);
    }
}
