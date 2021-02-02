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

public class ProjectionBuilder {

    private final Solution compositionSolution;
    private final CompositionData compositionData;
    private final List<WorkspaceEntry> wes;
    private final Map<WorkspaceEntry, Enabledness> componentEnablednessMap;
    private final Pair<String, Trace> violation;
    private final Map<WorkspaceEntry, Trace> componentViolationTracesMap;

    public ProjectionBuilder(Solution compositionSolution, CompositionData compositionData, List<WorkspaceEntry> wes) {
        this.compositionSolution = compositionSolution;
        this.compositionData = compositionData;
        this.wes = wes;
        componentEnablednessMap = calcWorkEnablednessMap();
        violation = calcViolation();
        componentViolationTracesMap = calcComponentViolationTraces();
    }

    private Map<WorkspaceEntry, Enabledness> calcWorkEnablednessMap() {
        Map<WorkspaceEntry, Enabledness> result = new HashMap<>();
        Set<Trace> compositionContinuations = compositionSolution.getContinuations();
        for (WorkspaceEntry we : wes) {
            ComponentData componentData = getComponentData(we);
            Enabledness enabledness = CompositionUtils.getEnabledness(compositionContinuations, componentData);
            result.put(we, enabledness);
        }
        return result;
    }

    private Pair<String, Trace> calcViolation() {
        Enabledness violationEnabledness = new Enabledness();
        for (WorkspaceEntry we : wes) {
            Enabledness enabledness = getComponentEnabledness(we);
            for (String violationOutputEvent : getUnexpectedlyEnabledOutputEvents(we)) {
                Trace trace = enabledness.get(violationOutputEvent);
                violationEnabledness.put(violationOutputEvent, trace);
            }
        }

        String violationEvent = violationEnabledness.isEmpty() ? null : violationEnabledness.keySet().iterator().next();
        Trace violationContinuation = violationEnabledness.getOrDefault(violationEvent, new Trace());
        Trace violationTrace = new Trace(compositionSolution.getMainTrace());
        violationTrace.addAll(violationContinuation);
        return Pair.of(violationEvent, violationTrace);
    }

    public Set<String> getUnexpectedlyEnabledOutputEvents(WorkspaceEntry we) {
        Set<String> result = new HashSet<>();

        StgModel stg = getComponentStg(we);
        Set<String> outputSignals = stg.getSignalReferences(Signal.Type.OUTPUT);
        result.addAll(StgUtils.getAllEvents(outputSignals));

        Enabledness enabledness = getComponentEnabledness(we);
        result.retainAll(enabledness.keySet());

        result.retainAll(getDisabledInputEvents());

        return result;
    }

    private String getViolationEvent() {
        return violation.getFirst();
    }

    private Trace getViolationTrace() {
        return violation.getSecond();
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

    private Set<String> getDisabledInputEvents() {
        Set<String> result = new HashSet<>();
        for (WorkspaceEntry we : wes) {
            Enabledness enabledness = getComponentEnabledness(we);

            StgModel stg = getComponentStg(we);
            Set<String> inputSignals = stg.getSignalReferences(Signal.Type.INPUT);
            Set<String> inputEvents = StgUtils.getAllEvents(inputSignals);
            inputEvents.removeAll(enabledness.keySet());

            result.addAll(inputEvents);
        }
        return result;
    }

    private Map<WorkspaceEntry, Trace> calcComponentViolationTraces() {
        Map<WorkspaceEntry, Trace> result = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            ComponentData componentData = getComponentData(we);
            Trace projectedTrace = new Trace();
            for (String ref : getViolationTrace()) {
                String srcRef = componentData == null ? null : componentData.getSrcTransition(ref);
                projectedTrace.add(srcRef);
            }
            result.put(we, projectedTrace);
        }
        return result;
    }

    public Trace calcCompositionViolationTrace() {
        Trace result = new Trace(getViolationTrace());
        for (WorkspaceEntry we : wes) {
            StgModel componentStg = getComponentStg(we);
            Trace componentTrace = getComponentViolationTrace(we);
            int i = 0;
            for (String ref : componentTrace) {
                if ((ref != null) && (i < result.size())) {
                    MathNode node = componentStg.getNodeByReference(ref);
                    boolean needsSubstitution = node != null;
                    if (node instanceof SignalTransition) {
                        SignalTransition st = (SignalTransition) node;
                        needsSubstitution = st.getSignalType() != Signal.Type.INPUT;
                    }
                    if (needsSubstitution) {
                        result.set(i, ref);
                    }
                }
                i++;
            }
        }
        String violationEvent = getViolationEvent();
        if (violationEvent != null) {
            result.add(violationEvent);
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
        Trace trace = getComponentViolationTrace(we);
        for (String ref : trace) {
            ProjectionEvent.Tag tag = getNodeTag(stg, ref);
            result.add(new ProjectionEvent(tag, ref));
        }

        String violationEvent = getViolationEvent();
        if (violationEvent != null) {
            Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(violationEvent);
            if (r != null) {
                String signal = r.getFirst();
                Set<String> outputs = stg.getSignalReferences(Signal.Type.OUTPUT);
                if (outputs.contains(signal)) {
                    result.add(new ProjectionEvent(ProjectionEvent.Tag.OUTPUT, violationEvent));
                } else if (stg.getSignalReferences(Signal.Type.INPUT).contains(signal)) {
                    SignalTransition.Direction direction = r.getSecond();
                    ProjectionEvent.Tag tag = getInputEventTag(we, trace, signal, direction);
                    result.add(new ProjectionEvent(tag, violationEvent));
                } else {
                    result.add(new ProjectionEvent(ProjectionEvent.Tag.NONE, null));
                }
            }
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
