package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.dom.Container;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.types.Triple;
import org.workcraft.utils.Hierarchy;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CompositionTransformer {

    static class ShadowConfig extends Triple<String, SignalTransition.Direction, Set<StgPlace>> {
        ShadowConfig(String signal, SignalTransition.Direction direction, Set<StgPlace> preset) {
            super(signal, direction, preset);
        }
    }

    private final Stg compositionStg;
    private final CompositionData compositionData;
    private final Map<ComponentData, Collection<StgPlace>> componentToPlacesMap;
    private final Map<String, Collection<SignalTransition>> signalToTransitionsMap;
    private final Set<ShadowConfig> shadowConfigs = new HashSet<>();

    public CompositionTransformer(Stg compositionStg, CompositionData compositionData) {
        this.compositionStg = compositionStg;
        this.compositionData = compositionData;
        // Store original places of each component
        componentToPlacesMap = new HashMap<>();
        for (String fileName : compositionData.getFileNames()) {
            ComponentData componentData = compositionData.getComponentData(fileName);
            Collection<StgPlace> componentPlaces = componentData.getDstPlaces().stream()
                    .map(compositionStg::getNodeByReference)
                    .filter(node -> node instanceof StgPlace)
                    .map(node -> (StgPlace) node)
                    .collect(Collectors.toSet());

            componentToPlacesMap.put(componentData, componentPlaces);
        }
        // Store original transitions of each signal
        signalToTransitionsMap = new HashMap<>();
        for (String signal : compositionStg.getSignalReferences()) {
            signalToTransitionsMap.put(signal, compositionStg.getSignalTransitions(signal));
        }
    }

    public Collection<SignalTransition> insetShadowTransitions(Collection<String> signals, File componentFile) {
        Collection<SignalTransition> result = new HashSet<>();
        for (String signal : signals) {
            ComponentData componentData = compositionData.getComponentData(componentFile);
            for (SignalTransition.Direction direction : SignalTransition.Direction.values()) {
                result.addAll(insetShadowTransitions(signal, direction, componentData));
            }
        }
        return result;
    }

    private Collection<SignalTransition> insetShadowTransitions(String signal,
            SignalTransition.Direction direction, ComponentData componentData) {

        Collection<SignalTransition> result = new HashSet<>();
        Collection<SignalTransition> signalTransitions = signalToTransitionsMap.getOrDefault(signal, new HashSet<>());
        Collection<StgPlace> componentPlaces = componentToPlacesMap.get(componentData);
        for (SignalTransition signalTransition : signalTransitions) {
            if (signalTransition.getDirection() == direction) {
                Set<StgPlace> componentPreset = new HashSet<>(componentPlaces);
                componentPreset.retainAll(compositionStg.getPreset(signalTransition, StgPlace.class));

                ShadowConfig shadowConfig = new ShadowConfig(signal, direction, componentPreset);
                if (!componentPreset.isEmpty() && !shadowConfigs.contains(shadowConfig)) {
                    shadowConfigs.add(shadowConfig);
                    Set<StgPlace> componentPostset = new HashSet<>(componentPlaces);
                    componentPostset.retainAll(compositionStg.getPostset(signalTransition, StgPlace.class));
                    SignalTransition shadowTransition = insetShadowTransition(signal, direction, componentPreset, componentPostset);
                    result.add(shadowTransition);
                    String shadowTransitionRef = compositionStg.getNodeReference(shadowTransition);
                    String signalTransitionRef = compositionStg.getNodeReference(signalTransition);
                    componentData.addShadowTransition(shadowTransitionRef, signalTransitionRef);
                }
            }
        }
        return result;
    }

    private SignalTransition insetShadowTransition(String signal,
            SignalTransition.Direction direction, Set<StgPlace> preset, Set<StgPlace> postset) {

        SignalTransition shadowTransition = compositionStg.createSignalTransition(signal, direction, null);
        for (StgPlace predPlace : preset) {
            compositionStg.makeExplicit(predPlace);
            try {
                compositionStg.connect(predPlace, shadowTransition);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        Set<StgPlace> readset = new HashSet<>(postset);
        readset.retainAll(preset);
        for (StgPlace readPlace : readset) {
            try {
                compositionStg.connect(shadowTransition, readPlace);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }

        if (readset.isEmpty()) {
            Container container = Hierarchy.getNearestContainer(shadowTransition);
            StgPlace shadowPlace = compositionStg.createPlace(null, container);
            try {
                compositionStg.connect(shadowTransition, shadowPlace);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        return shadowTransition;
    }

}
