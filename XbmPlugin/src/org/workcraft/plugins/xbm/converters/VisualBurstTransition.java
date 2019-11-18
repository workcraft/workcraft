package org.workcraft.plugins.xbm.converters;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.xbm.Burst;
import org.workcraft.plugins.xbm.Direction;
import org.workcraft.plugins.xbm.VisualBurstEvent;
import org.workcraft.plugins.xbm.XbmSignal;

import java.util.HashSet;
import java.util.Set;

public class VisualBurstTransition {

    private final VisualStg visualStg;
    private final Set<VisualSignalTransition> inputTransitions;
    private final Set<VisualSignalTransition> outputTransitions;
    private final VisualDummyTransition start;
    private final VisualDummyTransition split;
    private final VisualDummyTransition end;

    private static final String JOIN_PREFIX = "JOIN";
    private static final String FORK_PREFIX = "FORK";
    private static final String JOIN_FORK_PREFIX = JOIN_PREFIX + "_" + FORK_PREFIX;
    private static final String OUTPUT_DUMMY = "DUMMY_OUTPUT";

    public VisualBurstTransition(VisualStg target, VisualBurstEvent ref) {
        visualStg = target;
        inputTransitions = convertInputBursts(ref);
        outputTransitions = convertOutputBursts(ref);
        start = target.createVisualDummyTransition(FORK_PREFIX);
        split = target.createVisualDummyTransition(JOIN_FORK_PREFIX);
        end = target.createVisualDummyTransition(JOIN_PREFIX);
        try {
            for (VisualSignalTransition inputTransition: inputTransitions) {
                visualStg.connect(start, inputTransition);
                visualStg.connect(inputTransition, split);
            }
            if (!outputTransitions.isEmpty()) {
                for (VisualSignalTransition outputTransition: outputTransitions) {
                    visualStg.connect(split, outputTransition);
                    visualStg.connect(outputTransition, end);
                }
            } else {
                VisualDummyTransition internalChange = target.createVisualDummyTransition(OUTPUT_DUMMY);
                visualStg.connect(split, internalChange);
                visualStg.connect(internalChange, end);
            }
        } catch (InvalidConnectionException ice) {
            inputTransitions.clear();
            outputTransitions.clear();
        }
    }

    public Set<VisualSignalTransition> getInputTransitions() {
        return inputTransitions;
    }

    public Set<VisualSignalTransition> getOutputTransitions() {
        return outputTransitions;
    }

    public VisualDummyTransition getStart() {
        return start;
    }

    public VisualDummyTransition getSplit() {
        return split;
    }

    public VisualDummyTransition getEnd() {
        return end;
    }

    private Set<VisualSignalTransition> convertInputBursts(VisualBurstEvent ref) {
        return getBurstAsTransitions(ref, visualStg, XbmSignal.Type.INPUT);
    }

    private Set<VisualSignalTransition> convertOutputBursts(VisualBurstEvent ref) {
        return getBurstAsTransitions(ref, visualStg, XbmSignal.Type.OUTPUT);
    }

    private static Set<VisualSignalTransition> getBurstAsTransitions(VisualBurstEvent ref, VisualStg visualStg, XbmSignal.Type targetType) {
        Set<VisualSignalTransition> result = new HashSet<>();
        Burst burst = ref.getReferencedConnection().getBurst();
        for (XbmSignal signal: burst.getSignals(targetType)) {
            String name = signal.getName();
            XbmSignal.Type type = signal.getType();
            Direction direction = burst.getDirection().get(signal);
            VisualSignalTransition transition = visualStg.createVisualSignalTransition(name, XbmToStgConversionUtil.getReferredType(type), XbmToStgConversionUtil.getReferredDirection(direction));
            transition.setForegroundColor(ref.getColor());
            transition.setLabelColor(ref.getLabelColor());
            result.add(transition);
        }
        return result;
    }
}
