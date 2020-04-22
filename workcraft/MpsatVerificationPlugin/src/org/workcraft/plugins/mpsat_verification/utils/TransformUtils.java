package org.workcraft.plugins.mpsat_verification.utils;

import org.workcraft.dom.Container;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.utils.Hierarchy;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TransformUtils {

    public static Set<String> generateShadows(Stg compStg, CompositionData compositionData) {
        Set<String> result = new HashSet<>();
        for (String stgFileName : compositionData.getFileNames()) {
            ComponentData componentData = compositionData.getComponentData(stgFileName);
            Set<String> shadowTransitons = generateShadows(compStg, componentData);
            result.addAll(shadowTransitons);
        }
        return result;
    }

    public static Set<String> generateShadows(Stg compStg, ComponentData componentData) {
        Set<String> result = new HashSet<>();
        File stgFile = new File(componentData.getFileName());
        Stg stg = StgUtils.importStg(stgFile);
        Set<String> dstPlaceRefs = componentData.getDstPlaces();
        Set<String> localSignals = new HashSet<>();
        localSignals.addAll(stg.getSignalReferences(Signal.Type.OUTPUT));
        localSignals.addAll(stg.getSignalReferences(Signal.Type.INTERNAL));
        for (String signalRef : localSignals) {
            for (SignalTransition signalTransition : compStg.getSignalTransitions(signalRef)) {
                Set<StgPlace> srcPlaces = getSrcPlaces(compStg, signalTransition, dstPlaceRefs);
                if (!srcPlaces.isEmpty()) {
                    SignalTransition shadowTransition = createShadow(compStg, signalTransition, srcPlaces);
                    String shadowTransitionRef = compStg.getNodeReference(shadowTransition);
                    result.add(shadowTransitionRef);
                }
            }
        }
        return result;
    }

    private static SignalTransition createShadow(Stg compStg, SignalTransition signalTransition, Set<StgPlace> srcPlaces) {
        String signalName = signalTransition.getSignalName();
        SignalTransition.Direction direction = signalTransition.getDirection();
        Container container = Hierarchy.getNearestContainer(signalTransition);
        SignalTransition shadowTransition = compStg.createSignalTransition(signalName, direction, container);
        for (StgPlace srcPlace : srcPlaces) {
            compStg.makeExplicit(srcPlace);
            try {
                compStg.connect(srcPlace, shadowTransition);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        StgPlace shadowPlace = compStg.createPlace(null, container);
        try {
            compStg.connect(shadowTransition, shadowPlace);
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
        return shadowTransition;
    }

    private static Set<StgPlace> getSrcPlaces(Stg compStg, SignalTransition signalTransition, Set<String> dstPlaceRefs) {
        Set<StgPlace> srcPlaces = new HashSet<>();
        for (StgPlace place : compStg.getPreset(signalTransition, StgPlace.class)) {
            String dstPlaceRef = compStg.getNodeReference(place);
            if (dstPlaceRefs.contains(dstPlaceRef)) {
                srcPlaces.add(place);
            }
        }
        return srcPlaces;
    }

}
