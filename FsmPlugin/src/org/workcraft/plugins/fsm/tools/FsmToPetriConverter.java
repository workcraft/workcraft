package org.workcraft.plugins.fsm.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Hierarchy;

public class FsmToPetriConverter {
    private final VisualFsm srcModel;
    private final VisualPetriNet dstModel;

    private final Map<VisualState, VisualPlace> stateToPlaceMap;
    private final Map<VisualEvent, VisualTransition> eventToTransitionMap;
    private final Map<String, String> refToSymbolMap;

    public FsmToPetriConverter(VisualFsm srcModel, VisualPetriNet dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;
        stateToPlaceMap = convertStates();
        eventToTransitionMap = convertEvents();
        refToSymbolMap = cacheLabels();
        try {
            connectEvents();
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> cacheLabels() {
        Map<String, String> result = new HashMap<>();
        for (Entry<VisualEvent, VisualTransition> entry: eventToTransitionMap.entrySet()) {
            VisualEvent event = entry.getKey();
            VisualTransition transition = entry.getValue();
            Symbol symbol = event.getReferencedEvent().getSymbol();
            String dstName = dstModel.getMathName(transition);
            String srcName = (symbol == null) ? "" : srcModel.getMathName(symbol);
            result.put(dstName, srcName);
        }
        return result;
    }

    private Map<VisualState, VisualPlace> convertStates() {
        Map<VisualState, VisualPlace> result = new HashMap<>();
        for (VisualState state: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualState.class)) {
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

    private Map<VisualEvent, VisualTransition> convertEvents() {
        Map<VisualEvent, VisualTransition> result = new HashMap<>();
        HierarchicalUniqueNameReferenceManager refManager = (HierarchicalUniqueNameReferenceManager) dstModel.getPetriNet().getReferenceManager();
        NameManager nameManagerer = refManager.getNameManager(null);
        for (VisualEvent event : Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualEvent.class)) {
            Symbol symbol = event.getReferencedEvent().getSymbol();
            String symbolName = (symbol == null) ? Fsm.EPSILON_SERIALISATION : srcModel.getMathName(symbol);
            String name = nameManagerer.getDerivedName(null, symbolName);
            VisualTransition transition = dstModel.createTransition(name, null);
            transition.setPosition(event.getCenter());
            transition.setForegroundColor(event.getColor());
            if (symbol != null) {
                transition.setLabel(symbolName);
            }
            transition.setLabelColor(event.getLabelColor());
            result.put(event, transition);
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

    public VisualPetriNet getDstModel() {
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

    public String getSymbol(String ref) {
        return refToSymbolMap.get(ref);
    }

}
