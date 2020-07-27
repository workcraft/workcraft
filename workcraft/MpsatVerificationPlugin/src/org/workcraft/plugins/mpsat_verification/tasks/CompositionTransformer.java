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
import org.workcraft.utils.Hierarchy;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CompositionTransformer {

    private final Stg compositionStg;
    private final CompositionData compositionData;
    private final Map<ComponentData, Collection<StgPlace>> componentToPlacesMap;
    private final Map<String, Collection<SignalTransition>> signalToTransitionsMap;

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

    public Collection<SignalTransition> insetShadowTransitions(String signal, File componentFile) {
        Collection<SignalTransition> result = new HashSet<>();
        for (SignalTransition signalTransition : signalToTransitionsMap.get(signal)) {
            SignalTransition shadowTransition = insetShadowTransition(signalTransition, componentFile);
            if (shadowTransition != null) {
                result.add(shadowTransition);
            }
        }
        return result;
    }

    public SignalTransition insetShadowTransition(SignalTransition signalTransition, File componentFile) {
        ComponentData componentData = compositionData.getComponentData(componentFile);
        Set<StgPlace> componentPlaces = new HashSet<>(componentToPlacesMap.get(componentData));
        componentPlaces.retainAll(compositionStg.getPreset(signalTransition));
        if (componentPlaces.isEmpty()) {
            return null;
        }
        String signalName = signalTransition.getSignalName();
        SignalTransition.Direction direction = signalTransition.getDirection();
        Container container = Hierarchy.getNearestContainer(signalTransition);
        SignalTransition shadowTransition = compositionStg.createSignalTransition(signalName, direction, container);
        for (StgPlace componentPlace : componentPlaces) {
            compositionStg.makeExplicit(componentPlace);
            try {
                compositionStg.connect(componentPlace, shadowTransition);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        StgPlace shadowPlace = compositionStg.createPlace(null, container);
        try {
            compositionStg.connect(shadowTransition, shadowPlace);
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
        return shadowTransition;
    }

}
