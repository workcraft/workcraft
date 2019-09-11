package org.workcraft.plugins.xbm.converters;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.xbm.*;
import org.workcraft.utils.Hierarchy;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class XbmToStgConverter {

    private final VisualXbm srcModel;
    private final VisualStg dstModel;

    private final Map<VisualXbmState, VisualStgPlace> stateToPlaceMap;
    private final Map<VisualBurstEvent, VisualBurstTransition> burstEventToTransitionsMap;
    private final Map<XbmSignal, StgElementaryCycle> signalToElementaryCycleMap;

    private final static String IN_PREFIX = "_IN";
    private final static String OUT_PREFIX = "_OUT";

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


    private final Map<VisualXbmState, VisualStgPlace> convertStates() {
        Map<VisualXbmState, VisualStgPlace> result = new HashMap<>();
        for (VisualXbmState state: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualXbmState.class)) {
            String name = srcModel.getMathModel().getNodeReference(state.getReferencedState());
            VisualStgPlace place = dstModel.createVisualPlace(name, null);
            place.copyPosition(state);
            place.copyStyle(state);
            place.getReferencedPlace().setTokens(state.getReferencedState().isInitial() ? 1 : 0);
            place.setTokenColor(state.getForegroundColor());
            result.put(state, place);
        }
        return result;
    }

    private final Map<VisualBurstEvent, VisualBurstTransition> convertBurstEvents() {
        Map<VisualBurstEvent, VisualBurstTransition> result = new HashMap<>();
        for (VisualBurstEvent event: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualBurstEvent.class)) {
            VisualBurstTransition burstTransition = new VisualBurstTransition(event, dstModel);
            result.put(event, burstTransition);
        }
        return result;
    }

    private final Map<XbmSignal, StgElementaryCycle> convertSignalsToElementaryCycles() {
        Map<XbmSignal, StgElementaryCycle> result = new HashMap<>();
        for (XbmSignal xbmSignal : srcModel.getMathModel().getSignals(XbmSignal.Type.CONDITIONAL)) {
            result.put(xbmSignal, new StgElementaryCycle(dstModel, xbmSignal));
        }
        return result;
    }

    private final void connectBurstEvents() throws InvalidConnectionException {
        for (VisualBurstEvent event: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualBurstEvent.class)) {
            VisualBurstTransition burstTransition = burstEventToTransitionsMap.get(event);
            if (burstTransition != null) {
                Node first = event.getFirst();
                if (first instanceof VisualXbmState) {
                    VisualStgPlace inPlace = stateToPlaceMap.get(first);
                    VisualDummyTransition dummyOut = dstModel.createVisualDummyTransition(dstModel.getMathName(inPlace.getReferencedComponent()) + OUT_PREFIX);
                    dstModel.connect(inPlace, dummyOut);
                    if (inPlace != null) {
                        for (VisualSignalTransition inputTransition: burstTransition.getInputTransitions()) {
                            //dstModel.connect(inPlace, inputTransition);
                            dstModel.connect(dummyOut, inputTransition);
                        }
                    }
                }
                Node second = event.getSecond();
                if (second instanceof VisualXbmState) {
                    VisualPlace outPlace = stateToPlaceMap.get(second);
                    VisualDummyTransition dummyIn = dstModel.createVisualDummyTransition(dstModel.getMathName(outPlace.getReferencedPlace()) + IN_PREFIX);
                    dstModel.connect(dummyIn, outPlace);
                    if (outPlace != null) {
                        for (VisualSignalTransition outputTransition: burstTransition.getOutputTransitions()) {
                            //dstModel.connect(outputTransition, outPlace);
                            dstModel.connect(outputTransition, dummyIn);
                        }
                    }
                }
                if (event.getReferencedBurstEvent().hasConditional()) { //Connects the elementary cycle appropriate to the burst transition
                    for (Map.Entry<String, Boolean> condition: event.getReferencedBurstEvent().getConditionalMapping().entrySet()) {
                        VisualPlace readPlace;
                        StgElementaryCycle elemCycle = getRelatedStgElementaryCycle((XbmSignal) srcModel.getMathModel().getNodeByReference(condition.getKey()));
                        if (condition.getValue()) {
                            readPlace = elemCycle.getHigh();
                        } else {
                            readPlace = elemCycle.getLow();
                        }
                        for (VisualSignalTransition inputTransition : burstTransition.getInputTransitions()) {
                            dstModel.connectUndirected(readPlace, inputTransition);
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
}

class VisualBurstTransition {

    private final VisualStg visualStg;
    private final Set<VisualSignalTransition> inputTransitions;
    private final Set<VisualSignalTransition> outputTransitions;

    public VisualBurstTransition(VisualBurstEvent ref, VisualStg target) {
        visualStg = target;
        inputTransitions = convertInputBursts(ref);
        outputTransitions = convertOutputBursts(ref);

        try {
            for (VisualSignalTransition inputTransition: inputTransitions) {
                for (VisualSignalTransition outputTransition: outputTransitions) {
                    visualStg.connect(inputTransition, outputTransition);
                }
            }
        }
        catch (InvalidConnectionException ice) {
            //Remove the places if the check fails
            for (VisualSignalTransition inputTransition: inputTransitions) {
                visualStg.remove(inputTransition);
            }
            for (VisualSignalTransition outputTransition: outputTransitions) {
                visualStg.remove(outputTransition);
            }
        }
    }

    public Set<VisualSignalTransition> getInputTransitions() {
        return inputTransitions;
    }

    public Set<VisualSignalTransition> getOutputTransitions() {
        return outputTransitions;
    }

    private Set<VisualSignalTransition> convertInputBursts(VisualBurstEvent ref) {
        return getBurst(ref, visualStg, XbmSignal.Type.INPUT);
    }

    private Set<VisualSignalTransition> convertOutputBursts(VisualBurstEvent ref) {
        return getBurst(ref, visualStg, XbmSignal.Type.OUTPUT);
    }

    private static Set<VisualSignalTransition> getBurst(VisualBurstEvent ref, VisualStg visualStg, XbmSignal.Type targetType) {
        Set<VisualSignalTransition> result = new LinkedHashSet<>();
        Burst burst = ref.getReferencedBurstEvent().getBurst();
        for (XbmSignal input: burst.getSignals(targetType)) {
            VisualSignalTransition transition = visualStg.createVisualSignalTransition
                    (input.getName(), XbmToStgConversionUtil.getReferredType(input.getType()), XbmToStgConversionUtil.getReferredDirection(burst.getDirection().get(input)));
            transition.setPosition(ref.getCenter());
            transition.setForegroundColor(ref.getColor());
            transition.setLabelColor(ref.getLabelColor());
            result.add(transition);
        }
        return result;
    }
}

class StgElementaryCycle {

    public final static String TRANSITION_NAME_RISING = "_PLUS";
    public final static String TRANSITION_NAME_FALLING = "_MINUS";
    public final static String PLACE_NAME_LOW = "_LOW";
    public final static String PLACE_NAME_HIGH = "_HIGH";

    private final VisualStgPlace low, high;
    private final VisualSignalTransition falling, rising;

    public StgElementaryCycle(VisualStg visualStg, XbmSignal xbmSignal) {
        if (xbmSignal.getType() != XbmSignal.Type.DUMMY) {
            low = generateLowState(visualStg, xbmSignal);
            high = generateHighState(visualStg, xbmSignal);
            falling = generateFallingTransition(visualStg, xbmSignal);
            rising = generateRisingTransition(visualStg, xbmSignal);

            try {
                //Elementary cycle
                visualStg.connect(low, rising);
                visualStg.connect(rising, high);
                visualStg.connect(high, falling);
                visualStg.connect(falling, low);
            }
            catch (InvalidConnectionException ice) {
                //Remove the places if the check fails
                visualStg.remove(low);
                visualStg.remove(high);
                visualStg.remove(falling);
                visualStg.remove(rising);
            }
        }
        else {
            low = null;
            high = null;
            falling = null;
            rising = null;
        }
    }

    public VisualPlace getLow() {
        return low;
    }

    public VisualPlace getHigh() {
        return high;
    }

    public VisualTransition getFalling() {
        return falling;
    }

    public VisualTransition getRising() {
        return rising;
    }

    private static final VisualStgPlace generateLowState(VisualStg visualStg, XbmSignal xbmSignal) {
        return createPlace(visualStg, xbmSignal, PLACE_NAME_LOW, 1);
    }

    private static final VisualStgPlace generateHighState(VisualStg visualStg, XbmSignal xbmSignal) {
        return createPlace(visualStg, xbmSignal, PLACE_NAME_HIGH, 0);
    }

    private static final VisualSignalTransition generateFallingTransition(VisualStg visualStg, XbmSignal xbmSignal) {
        return createTransition(visualStg, xbmSignal, TRANSITION_NAME_FALLING);
    }

    private static final VisualSignalTransition generateRisingTransition(VisualStg visualStg, XbmSignal xbmSignal) {
        return createTransition(visualStg, xbmSignal, TRANSITION_NAME_RISING);
    }

    private static VisualStgPlace createPlace(VisualStg visualStg, XbmSignal xbmSignal, String namePrefix, int tokenCount) {
        final VisualStgPlace result = visualStg.createVisualPlace(xbmSignal.getName() + namePrefix, null);
        final String prefixParse = namePrefix.equals(PLACE_NAME_HIGH) ? "=1" : "=0";
        result.getReferencedComponent().setTokens(tokenCount);
        result.setLabel(xbmSignal.getName() + prefixParse);
        return result;
    }

    private static final VisualSignalTransition createTransition(VisualStg visualStg, XbmSignal xbmSignal, String namePrefix) {
        final SignalTransition.Direction determineDirection = namePrefix.equals(TRANSITION_NAME_RISING) ? SignalTransition.Direction.PLUS : SignalTransition.Direction.MINUS;
        final VisualSignalTransition result = visualStg.createVisualSignalTransition
                (xbmSignal.getName() + namePrefix, XbmToStgConversionUtil.getReferredType(xbmSignal.getType()), determineDirection);
        return result;
    }
}

class XbmToStgConversionUtil {
    public static Signal.Type getReferredType(XbmSignal.Type burstType) {
        switch (burstType) {
            case INPUT:
                return Signal.Type.INPUT;
            case OUTPUT:
                return Signal.Type.OUTPUT;
            case CONDITIONAL:
                return Signal.Type.INTERNAL;
            default:
                return null;
        }
    }

    public static SignalTransition.Direction getReferredDirection(Burst.Direction burstDirection) {
        switch (burstDirection) {
            case PLUS:
                return SignalTransition.Direction.PLUS;
            case MINUS:
                return SignalTransition.Direction.MINUS;
            default:
                return SignalTransition.Direction.TOGGLE; //TODO Add conversion for XBM's stable and unstable signals
        }
    }
}