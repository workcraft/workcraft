package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.dom.Container;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.utils.StgUtils;
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

    public Collection<SignalTransition> insetShadowTransitions() {
        Collection<SignalTransition> result = new HashSet<>();
        for (String componentFileName : compositionData.getFileNames()) {
            File componentFile = new File(componentFileName);
            result.addAll(insetShadowTransitions(componentFile));
        }
        return result;
    }

    public Collection<SignalTransition> insetShadowTransitions(File componentFile) {
        Stg componentStg = StgUtils.importStg(componentFile);
        Set<String> localSignals = new HashSet<>();
        localSignals.addAll(componentStg.getSignalReferences(Signal.Type.OUTPUT));
        localSignals.addAll(componentStg.getSignalReferences(Signal.Type.INTERNAL));
        return insetShadowTransitions(localSignals, componentFile);
    }

    public Collection<SignalTransition> insetShadowTransitions(Collection<String> signals, File componentFile) {
        Collection<SignalTransition> result = new HashSet<>();
        for (String signal : signals) {
            result.addAll(insetShadowTransitions(signal, componentFile));
        }
        return result;
    }

    private Collection<SignalTransition> insetShadowTransitions(String signal, File componentFile) {
        Collection<SignalTransition> result = new HashSet<>();
        ComponentData componentData = compositionData.getComponentData(componentFile);
        Set<StgPlace> componentPlaces = new HashSet<>(componentToPlacesMap.get(componentData));
        for (SignalTransition.Direction direction : SignalTransition.Direction.values()) {
            result.addAll(insetShadowTransitions(signal, direction, componentPlaces));
        }
        return result;
    }

    private Collection<SignalTransition> insetShadowTransitions(String signal,
            SignalTransition.Direction direction, Set<StgPlace> componentPlaces) {

        Collection<SignalTransition> result = new HashSet<>();
        Collection<SignalTransition> signalTransitions = signalToTransitionsMap.getOrDefault(signal, new HashSet<>());
        for (SignalTransition signalTransition : signalTransitions) {
            if (signalTransition.getDirection() == direction) {
                Set<StgPlace> componentPreset = new HashSet<>(componentPlaces);
                componentPreset.retainAll(compositionStg.getPreset(signalTransition));
                ShadowConfig shadowConfig = new ShadowConfig(signal, direction, componentPreset);
                if (!componentPreset.isEmpty() && !shadowConfigs.contains(shadowConfig)) {
                    shadowConfigs.add(shadowConfig);
                    result.add(insetShadowTransition(signal, direction, componentPreset));
                }
            }
        }
        return result;
    }

    private SignalTransition insetShadowTransition(String signal,
            SignalTransition.Direction direction, Set<StgPlace> preset) {

        SignalTransition shadowTransition = compositionStg.createSignalTransition(signal, direction, null);
        for (StgPlace place : preset) {
            compositionStg.makeExplicit(place);
            try {
                compositionStg.connect(place, shadowTransition);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        Container container = Hierarchy.getNearestContainer(shadowTransition);
        StgPlace shadowPlace = compositionStg.createPlace(null, container);
        try {
            compositionStg.connect(shadowTransition, shadowPlace);
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
        return shadowTransition;
    }

}
