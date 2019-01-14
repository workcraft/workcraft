package org.workcraft.plugins.mpsat.utils;

import org.workcraft.dom.Container;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.util.Hierarchy;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TransformUtils {

    public static Set<String> generateShadowTransitions(Stg compStg, CompositionData compositionData) {
        Set<String> result = new HashSet<>();
        for (String stgFileName : compositionData.getFileNames()) {
            ComponentData componentData = compositionData.getComponentData(stgFileName);
            Set<String> shadowTransitons = generateShadowTransitions(compStg, componentData);
            result.addAll(shadowTransitons);
        }
        return result;
    }

    public static Set<String> generateShadowTransitions(Stg compStg, ComponentData componentData) {
        Set<String> result = new HashSet<>();
        File stgFile = new File(componentData.getFileName());
        Stg stg = StgUtils.loadStg(stgFile);
        Set<String> dstPlaceRefs = componentData.getDstPlaces();
        Set<String> localSignals = new HashSet<>();
        localSignals.addAll(stg.getSignalReferences(Signal.Type.OUTPUT));
        localSignals.addAll(stg.getSignalReferences(Signal.Type.INTERNAL));
        for (String signalRef : localSignals) {
            for (SignalTransition signalTransition : compStg.getSignalTransitions(signalRef)) {
                Set<StgPlace> srcPlaces = new HashSet<>();
                for (StgPlace place : compStg.getPreset(signalTransition, StgPlace.class)) {
                    String dstPlaceRef = compStg.getNodeReference(place);
                    if (dstPlaceRefs.contains(dstPlaceRef)) {
                        srcPlaces.add(place);
                    }
                }
                if (!srcPlaces.isEmpty()) {
                    String signalName = signalTransition.getSignalName();
                    SignalTransition.Direction direction = signalTransition.getDirection();
                    Container container = Hierarchy.getNearestContainer(signalTransition);
                    SignalTransition shadowTransition = compStg.createSignalTransition(signalName, direction, container);
                    result.add(compStg.getNodeReference(shadowTransition));
                    for (StgPlace srcPlace : srcPlaces) {
                        compStg.makeExplicit(srcPlace);
                        try {
                            compStg.connect(srcPlace, shadowTransition);
                        } catch (InvalidConnectionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return result;
    }

}
