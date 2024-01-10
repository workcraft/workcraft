package org.workcraft.plugins.mpsat_verification.projection;

import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Pair;
import org.workcraft.types.Triple;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;
import java.util.stream.Collectors;

public class ProjectionBuilder {

    private final Solution compositionSolution;
    private final CompositionData compositionData;
    private final List<WorkspaceEntry> wes;
    private final Map<WorkspaceEntry, Map<String, String>> componentSignalRenameMap;
    private final Map<WorkspaceEntry, Enabledness> componentEnablednessMap;
    private final Pair<String, Trace> compositionViolation;
    private final Map<WorkspaceEntry, Trace> componentViolationTracesMap;

    public ProjectionBuilder(Solution compositionSolution, CompositionData compositionData, List<WorkspaceEntry> wes) {
        this.compositionSolution = compositionSolution;
        this.compositionData = compositionData;
        this.wes = wes;
        componentSignalRenameMap = calcComponentSignalRenameMap();
        componentEnablednessMap = calcComponentEnablednessMap();
        compositionViolation = calcCompositionViolation();
        componentViolationTracesMap = calcComponentViolationTraces();
    }

    private Map<WorkspaceEntry, Map<String, String>> calcComponentSignalRenameMap() {
        Map<WorkspaceEntry, Map<String, String>> result = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            result.put(we, calcComponentToCompositionSignalMap(we));
        }
        return result;
    }

    private Map<String, String> calcComponentToCompositionSignalMap(WorkspaceEntry we) {
        Map<String, String> result = new HashMap<>();
        StgModel componentStg = getComponentStg(we);
        ComponentData componentData = getComponentData(we);
        for (SignalTransition componentTransition : componentStg.getSignalTransitions()) {
            String componentTransitionRef = componentStg.getNodeReference(componentTransition);
            Triple<String, SignalTransition.Direction, Integer> componentTransitionTriple
                    = LabelParser.parseSignalTransition(componentTransitionRef);

            if ((componentTransitionTriple != null) && !result.containsKey(componentTransitionTriple.getFirst())) {
                String compositionSignalRef = calcCompositionSignalForComponentTransition(componentData, componentTransitionRef);
                if (compositionSignalRef != null) {
                    String componentSignalRef = componentTransitionTriple.getFirst();
                    result.put(componentSignalRef, compositionSignalRef);
                }
            }
        }
        return result;
    }

    private String calcCompositionSignalForComponentTransition(ComponentData componentData, String componentTransitionRef) {
        for (String dstTransitionRef : componentData.getDstTransitions()) {
            String srcTransitionRef = componentData.getSrcTransition(dstTransitionRef);
            if ((srcTransitionRef != null) && srcTransitionRef.equals(componentTransitionRef)) {

                Triple<String, SignalTransition.Direction, Integer> compositionTransitionTriple
                        = LabelParser.parseSignalTransition(dstTransitionRef);

                if (compositionTransitionTriple != null) {
                    return compositionTransitionTriple.getFirst();
                }
            }
        }
        return null;
    }

    private Map<WorkspaceEntry, Enabledness> calcComponentEnablednessMap() {
        Map<WorkspaceEntry, Enabledness> result = new HashMap<>();
        Set<Trace> compositionContinuations = compositionSolution.getContinuations();
        for (WorkspaceEntry we : wes) {
            ComponentData componentData = getComponentData(we);
            Enabledness enabledness = CompositionUtils.getEnabledness(compositionContinuations, componentData);
            result.put(we, enabledness);
        }
        return result;
    }

    private Pair<String, Trace> calcCompositionViolation() {
        String compositionViolationEvent = null;
        for (WorkspaceEntry we : wes) {
            Set<String> componentViolationEvents = getUnexpectedlyEnabledLocalEvents(we);
            if (!componentViolationEvents.isEmpty()) {
                String componentViolationEvent = componentViolationEvents.iterator().next();
                compositionViolationEvent = getCompositionEvent(we, componentViolationEvent);
                if (compositionViolationEvent != null) {
                    break;
                }
            }
        }
        Trace compositionViolationTrace = new Trace(compositionSolution.getMainTrace());
        return Pair.of(compositionViolationEvent, compositionViolationTrace);
    }

    private String getCompositionEvent(WorkspaceEntry we, String transitionRef) {
        Triple<String, SignalTransition.Direction, Integer> componentTriple = LabelParser.parseSignalTransition(transitionRef);
        if (componentTriple != null) {
            String componentSignal = componentTriple.getFirst();
            Map<String, String> componentToCompositionSignalMap = componentSignalRenameMap.getOrDefault(we, Collections.emptyMap());
            String compositionSignal = componentToCompositionSignalMap.get(componentSignal);
            if (compositionSignal != null) {
                return compositionSignal + componentTriple.getSecond();
            }
        }
        return null;
    }

    public Set<String> getUnexpectedlyEnabledLocalEvents(WorkspaceEntry we) {
        StgModel componentStg = getComponentStg(we);
        Set<String> localSignals = componentStg.getSignalReferences(Signal.Type.OUTPUT);
        localSignals.addAll(componentStg.getSignalReferences(Signal.Type.INTERNAL));
        Set<String> result = new HashSet<>(StgUtils.getAllEvents(localSignals));

        Enabledness enabledness = getComponentEnabledness(we);
        result.retainAll(enabledness.keySet());

        Set<String> enabledLocalCompositionEvents = new HashSet<>();
        Map<String, String> compositionToComponentEventMap = new HashMap<>();
        for (String componentEvent : result) {
            String compositionEvent = getCompositionEvent(we, componentEvent);
            if (compositionEvent != null) {
                enabledLocalCompositionEvents.add(compositionEvent);
                compositionToComponentEventMap.put(compositionEvent, componentEvent);
            }
        }
        Set<String> disabledInputCompositionEvents = getDisabledInputCompositionEvents(enabledLocalCompositionEvents);
        Set<String> disabledComponentEvents = disabledInputCompositionEvents.stream()
                .map(compositionToComponentEventMap::get).collect(Collectors.toSet());

        result.retainAll(disabledComponentEvents);
        return result;
    }

    private String getCompositionViolationEventRef() {
        return compositionViolation.getFirst();
    }

    private Trace getCompositionViolationTrace() {
        return compositionViolation.getSecond();
    }

    public ComponentData getComponentData(WorkspaceEntry we) {
        return compositionData == null ? null : compositionData.getComponentData(wes.indexOf(we));
    }

    public StgModel getComponentStg(WorkspaceEntry we) {
        return WorkspaceUtils.getAs(we, StgModel.class);
    }

    public Enabledness getComponentEnabledness(WorkspaceEntry we) {
        return componentEnablednessMap.get(we);
    }

    public Trace getComponentViolationTrace(WorkspaceEntry we) {
        return componentViolationTracesMap.get(we);
    }

    private Set<String> getDisabledInputCompositionEvents(Set<String> compositionEvents) {
        Set<String> result = new HashSet<>();
        for (WorkspaceEntry we : wes) {
            StgModel componentStg = getComponentStg(we);
            Set<String> componentInputSignals = componentStg.getSignalReferences(Signal.Type.INPUT);
            Map<String, String> componentToCompositionSignalMap = componentSignalRenameMap.getOrDefault(we, Collections.emptyMap());
            Set<String> compositionInputSignals = componentInputSignals.stream().map(componentToCompositionSignalMap::get).collect(Collectors.toSet());
            Set<String> compositionInputEvents = StgUtils.getAllEvents(compositionInputSignals);
            compositionInputEvents.retainAll(compositionEvents);

            Enabledness componentEnabledness = getComponentEnabledness(we);
            for (String componentEnabledEvent : componentEnabledness.keySet()) {
                String compositionEnabledEvent = getCompositionEvent(we, componentEnabledEvent);
                if (componentEnabledEvent != null) {
                    compositionInputEvents.remove(compositionEnabledEvent);
                }
            }
            result.addAll(compositionInputEvents);
        }
        return result;
    }

    private Map<WorkspaceEntry, Trace> calcComponentViolationTraces() {
        Map<WorkspaceEntry, Trace> result = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            ComponentData componentData = getComponentData(we);
            Trace projectedTrace = new Trace();
            for (String compositionRef : getCompositionViolationTrace()) {
                String componentRef = componentData == null ? null : componentData.getSrcTransition(compositionRef);
                projectedTrace.add(componentRef);
            }
            result.put(we, projectedTrace);
        }
        return result;
    }

    public Trace getCompositionTraceWithViolationEvent() {
        Trace result = new Trace(getCompositionViolationTrace());
        String compositionViolationEventRef = getCompositionViolationEventRef();
        if (compositionViolationEventRef != null) {
            result.add(compositionViolationEventRef);
        }
        return result;
    }

    public Map<WorkspaceEntry, ProjectionTrace> calcComponentProjectionTraces() {
        Map<WorkspaceEntry, ProjectionTrace> result = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            ProjectionTrace projectionTrace = calcComponentProjectionTrace(we);
            result.put(we, projectionTrace);
        }
        return result;
    }

    private ProjectionTrace calcComponentProjectionTrace(WorkspaceEntry we) {
        ProjectionTrace result = new ProjectionTrace();
        StgModel stg = getComponentStg(we);
        ComponentData componentData = getComponentData(we);
        Trace trace = getComponentViolationTrace(we);
        for (String ref : trace) {
            ProjectionEvent.Tag tag = getNodeTag(stg, ref);
            ProjectionEvent projectionEvent = new ProjectionEvent(tag, ref);
            result.add(projectionEvent);
        }

        String compositionViolationEventRef = getCompositionViolationEventRef();
        if (compositionViolationEventRef != null) {
            ProjectionEvent projectionEvent = new ProjectionEvent(ProjectionEvent.Tag.NONE, null);
            String projectionViolationEventRef = componentData.getSrcTransition(compositionViolationEventRef);
            if (projectionViolationEventRef != null) {
                Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(projectionViolationEventRef);
                if (r != null) {
                    String signal = r.getFirst();
                    if (stg.getSignalReferences(Signal.Type.OUTPUT).contains(signal)) {
                        projectionEvent = new ProjectionEvent(ProjectionEvent.Tag.OUTPUT, projectionViolationEventRef);
                    } else if (stg.getSignalReferences(Signal.Type.INTERNAL).contains(signal)) {
                        projectionEvent = new ProjectionEvent(ProjectionEvent.Tag.INTERNAL, projectionViolationEventRef);
                    } else if (stg.getSignalReferences(Signal.Type.INPUT).contains(signal)) {
                        SignalTransition.Direction direction = r.getSecond();
                        ProjectionEvent.Tag tag = getInputEventTag(we, trace, signal, direction);
                        projectionEvent = new ProjectionEvent(tag, projectionViolationEventRef);
                    }
                }
            }
            result.add(projectionEvent);
        }
        return result;
    }

    private ProjectionEvent.Tag getNodeTag(StgModel stg, String ref) {
        if (ref != null) {
            MathNode node = stg.getNodeByReference(ref);
            if (node instanceof DummyTransition) {
                return ProjectionEvent.Tag.DUMMY;
            } else if (node instanceof SignalTransition) {
                switch (((SignalTransition) node).getSignalType()) {
                case INPUT: return ProjectionEvent.Tag.INPUT;
                case OUTPUT: return ProjectionEvent.Tag.OUTPUT;
                case INTERNAL: return ProjectionEvent.Tag.INTERNAL;
                }
            }
        }
        return ProjectionEvent.Tag.NONE;
    }

    private ProjectionEvent.Tag getInputEventTag(WorkspaceEntry we, Trace componentTrace,
            String signal, SignalTransition.Direction direction) {

        StgModel cloneStg = WorkspaceUtils.getAs(WorkUtils.cloneModel(we.getModelEntry()), StgModel.class);
        if (PetriUtils.fireTrace(cloneStg, componentTrace)) {
            for (Transition t : PetriUtils.getEnabledTransitions(cloneStg)) {
                if (t instanceof SignalTransition) {
                    SignalTransition st = (SignalTransition) t;
                    if (signal.equals(st.getSignalName()) && (st.getDirection() == direction)) {
                        return ProjectionEvent.Tag.INPUT;
                    }
                }
            }
            return ProjectionEvent.Tag.VIOLATION;
        }
        return ProjectionEvent.Tag.NONE;
    }

}
