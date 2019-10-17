package org.workcraft.plugins.xbm.converters;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.VisualStgPlace;
import org.workcraft.plugins.xbm.*;
import org.workcraft.utils.Hierarchy;

import java.util.HashMap;
import java.util.Map;

public class XbmToStgConverter {

    private final VisualXbm srcModel;
    private final VisualStg dstModel;

    private final Map<VisualXbmState, VisualStgPlace> stateToPlaceMap;
    private final Map<VisualBurstEvent, VisualBurstTransition> burstEventToTransitionsMap;
    private final Map<XbmSignal, StgElementaryCycle> signalToElementaryCycleMap;

    public XbmToStgConverter(VisualXbm srcModel, VisualStg dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;

        this.stateToPlaceMap = convertStates();
        this.burstEventToTransitionsMap = convertBurstEvents();
        this.signalToElementaryCycleMap = convertSignalsToElementaryCycles();

        try {
            connectBurstEvents();
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<VisualXbmState, VisualStgPlace> convertStates() {
        Map<VisualXbmState, VisualStgPlace> result = new HashMap<>();
        for (VisualXbmState state: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualXbmState.class)) {
            String name = srcModel.getMathModel().getNodeReference(state.getReferencedComponent());
            VisualStgPlace place = dstModel.createVisualPlace(name, null);
            place.copyPosition(state);
            place.copyStyle(state);
            place.getReferencedComponent().setTokens(state.getReferencedComponent().isInitial() ? 1 : 0);
            place.setTokenColor(state.getForegroundColor());
            result.put(state, place);
        }
        return result;
    }

    private Map<VisualBurstEvent, VisualBurstTransition> convertBurstEvents() {
        Map<VisualBurstEvent, VisualBurstTransition> result = new HashMap<>();
        for (VisualBurstEvent event: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualBurstEvent.class)) {
            VisualBurstTransition burstTransition = new VisualBurstTransition(dstModel, event);
            result.put(event, burstTransition);
        }
        return result;
    }

    private Map<XbmSignal, StgElementaryCycle> convertSignalsToElementaryCycles() {
        Map<XbmSignal, StgElementaryCycle> result = new HashMap<>();
        for (XbmSignal xbmSignal : srcModel.getMathModel().getSignals(XbmSignal.Type.CONDITIONAL)) {
            result.put(xbmSignal, new StgElementaryCycle(dstModel, xbmSignal));
        }
        return result;
    }

    private void connectBurstEvents() throws InvalidConnectionException {
        for (VisualBurstEvent event: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualBurstEvent.class)) {
            VisualBurstTransition burstTransition = burstEventToTransitionsMap.get(event);
            if (burstTransition != null) {
                Node first = event.getFirst();
                if (first instanceof VisualXbmState) {
                    VisualStgPlace inPlace = stateToPlaceMap.get(first);
                    if (inPlace != null) {
                        if (burstTransition.getStart() != null) {
                            dstModel.connect(inPlace, burstTransition.getStart());
                        } else {
                            for (VisualSignalTransition inputTransition: burstTransition.getInputTransitions()) {
                                dstModel.connect(inPlace, inputTransition);
                            }
                        }
                        if (event.getReferencedConnection().hasConditional()) { //Connects the elementary cycle appropriate to the burst transition
                            for (Map.Entry<String, Boolean> condition: event.getReferencedConnection().getConditionalMapping().entrySet()) {
                                VisualStgPlace readPlace;
                                StgElementaryCycle elemCycle = getRelatedStgElementaryCycle((XbmSignal) srcModel.getMathModel().getNodeByReference(condition.getKey()));
                                if (condition.getValue()) {
                                    readPlace = elemCycle.getHigh();
                                } else {
                                    readPlace = elemCycle.getLow();
                                }
                                if (burstTransition.getStart() != null) {
                                    dstModel.connectUndirected(readPlace, burstTransition.getStart());
                                } else {
                                    for (VisualSignalTransition inputTransition: burstTransition.getInputTransitions()) {
                                        dstModel.connectUndirected(readPlace, inputTransition);
                                    }
                                }
                            }
                        }
                    }
                }
                Node second = event.getSecond();
                if (second instanceof VisualXbmState) {
                    VisualPlace outPlace = stateToPlaceMap.get(second);
                    if (outPlace != null) {
                        if (burstTransition.getEnd() != null) {
                            dstModel.connect(burstTransition.getEnd(), outPlace);
                        } else {
                            for (VisualSignalTransition outputTransition: burstTransition.getOutputTransitions()) {
                                dstModel.connect(outputTransition, outPlace);
                            }
                        }
                    }
                }
            }
        }
    }

    public VisualXbm getSrcModel() {
        return srcModel;
    }

    public VisualStg getDstModel() {
        return dstModel;
    }

    public VisualStgPlace getRelatedPlace(VisualXbmState state) {
        return stateToPlaceMap.get(state);
    }

    public VisualBurstTransition getRelatedSignalBurstTransition(VisualEvent event) {
        return burstEventToTransitionsMap.get(event);
    }

    public VisualXbmState getRelatedState(VisualPlace place) {
        for (Map.Entry<VisualXbmState, VisualStgPlace> entry: stateToPlaceMap.entrySet()) {
            if (entry.getValue() == place) {
                return entry.getKey();
            }
        }
        return null;
    }

    public VisualBurstEvent getRelatedEvent(VisualBurstTransition transition) {
        for (Map.Entry<VisualBurstEvent, VisualBurstTransition> entry: burstEventToTransitionsMap.entrySet()) {
            if (entry.getValue() == transition) {
                return entry.getKey();
            }
        }
        return null;
    }

    public StgElementaryCycle getRelatedStgElementaryCycle(XbmSignal conditional) {
        return signalToElementaryCycleMap.get(conditional);
    }

    public boolean isRelated(Node highLevelNode, Node node) {
        boolean result = false;
        if (highLevelNode instanceof VisualBurstEvent) {
            VisualBurstTransition relatedTransition = getRelatedSignalBurstTransition((VisualBurstEvent) highLevelNode);
            if (relatedTransition != null) {
                boolean allSignalsFound = true;
                if (node instanceof VisualBurstEvent) {
                    VisualBurstEvent vbe = (VisualBurstEvent) node;
                    for (XbmSignal s: vbe.getReferencedConnection().getBurst().getSignals()) {
                        boolean isInput = false;
                        for (VisualSignalTransition i: relatedTransition.getInputTransitions()) {
                            isInput = isInput || i.getName().equals(s.getName());
                        }
                        boolean isOutput = false;
                        for (VisualSignalTransition o: relatedTransition.getOutputTransitions()) {
                            isOutput = isOutput || o.getName().equals(s.getName());
                        }
                        allSignalsFound = true && (isInput || isOutput);
                    }
                } else if (node instanceof BurstEvent) {
                    BurstEvent be = (BurstEvent) node;
                    for (XbmSignal s: be.getBurst().getSignals()) {
                        boolean isInput = false;
                        for (VisualSignalTransition i: relatedTransition.getInputTransitions()) {
                            isInput = isInput || i.getName().equals(s.getName());
                        }
                        boolean isOutput = false;
                        for (VisualSignalTransition o: relatedTransition.getOutputTransitions()) {
                            isOutput = isOutput || o.getName().equals(s.getName());
                        }
                        allSignalsFound = true && (isInput || isOutput);
                    }
                }
                result = allSignalsFound;
            }
        } else if (highLevelNode instanceof VisualXbmState) {
            VisualPlace relatedPlace = getRelatedPlace((VisualXbmState) highLevelNode);
            if (relatedPlace != null) {
                result = (node == relatedPlace) || (node == relatedPlace.getReferencedComponent());
            }
        }
        return result;
    }
}