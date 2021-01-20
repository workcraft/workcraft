package org.workcraft.plugins.mpsat_verification.utils;

import org.workcraft.dom.math.MathNode;
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

public class CompositionEnabledness {

    public enum Tag {
        INPUT("input"),
        OUTPUT("output"),
        INTERNAL("internal"),
        DUMMY("dummy"),
        VIOLATION("violation"),
        NONE(null);

        private final String name;

        Tag(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Solution compositionSolution;
    private final CompositionData compositionData;
    private final List<WorkspaceEntry> wes;
    private final Map<WorkspaceEntry, Map<String, Trace>> componentEnablednessMap;
    private final Pair<String, Trace> violation;
    private final Map<WorkspaceEntry, Trace> componentViolationTracesMap;

    public CompositionEnabledness(Solution compositionSolution, CompositionData compositionData, List<WorkspaceEntry> wes) {
        this.compositionSolution = compositionSolution;
        this.compositionData = compositionData;
        this.wes = wes;
        componentEnablednessMap = calcWorkEnablednessMap();
        violation = calcViolation();
        componentViolationTracesMap = calcComponentViolationTraces();
    }

    private Map<WorkspaceEntry, Map<String, Trace>> calcWorkEnablednessMap() {
        Map<WorkspaceEntry, Map<String, Trace>> result = new HashMap<>();
        Set<Trace> compositionContinuations = compositionSolution.getContinuations();
        for (WorkspaceEntry we : wes) {
            ComponentData componentData = getComponentData(we);
            Map<String, Trace> enabledness = CompositionUtils.getEnabledness(compositionContinuations, componentData);
            result.put(we, enabledness);
        }
        return result;
    }

    private Pair<String, Trace> calcViolation() {
        Map<String, Trace> violationEnabledness = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            Map<String, Trace> enabledness = getComponentEnabledness(we);
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

        Map<String, Trace> enabledness = getComponentEnabledness(we);
        result.retainAll(enabledness.keySet());

        result.retainAll(getDisabledInputEvents());

        return result;
    }

    public String getViolationEvent() {
        return violation.getFirst();
    }

    public Trace getViolationTrace() {
        return violation.getSecond();
    }

    public ComponentData getComponentData(WorkspaceEntry we) {
        return compositionData == null ? null : compositionData.getComponentData(wes.indexOf(we));
    }

    public StgModel getComponentStg(WorkspaceEntry we) {
        return WorkspaceUtils.getAs(we, StgModel.class);
    }

    public Map<String, Trace> getComponentEnabledness(WorkspaceEntry we) {
        return componentEnablednessMap.get(we);
    }

    public Trace getComponentViolationTrace(WorkspaceEntry we) {
        return componentViolationTracesMap.get(we);
    }

    private Set<String> getDisabledInputEvents() {
        Set<String> result = new HashSet<>();
        for (WorkspaceEntry we : wes) {
            Map<String, Trace> enabledness = getComponentEnabledness(we);

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

    public Map<WorkspaceEntry, List<Tag>> calcComponentTagTraces() {

        Map<WorkspaceEntry, List<Tag>> result = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            List<Tag> tagTrace = calcComponentTagTrace(we);
            result.put(we, tagTrace);
        }
        return result;
    }

    private List<Tag> calcComponentTagTrace(WorkspaceEntry we) {
        List<Tag> result = new ArrayList<>();
        StgModel stg = getComponentStg(we);
        Trace trace = getComponentViolationTrace(we);
        for (String ref : trace) {
            Tag tag = getNodeTag(stg, ref);
            result.add(tag);
        }

        String violationEvent = getViolationEvent();
        if (violationEvent != null) {
            Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(violationEvent);
            if (r != null) {
                String signal = r.getFirst();
                Set<String> outputs = stg.getSignalReferences(Signal.Type.OUTPUT);
                Tag tag = Tag.NONE;
                if (outputs.contains(signal)) {
                    tag = Tag.OUTPUT;
                } else {
                    Set<String> inputs = stg.getSignalReferences(Signal.Type.INPUT);
                    if (inputs.contains(signal)) {
                        SignalTransition.Direction direction = r.getSecond();
                        tag = getInputEventTag(we, trace, signal, direction);
                    }
                }
                result.add(tag);
            }
        }
        return result;
    }

    private Tag getNodeTag(StgModel stg, String ref) {
        if (ref != null) {
            MathNode node = stg.getNodeByReference(ref);
            if (node instanceof DummyTransition) {
                return Tag.DUMMY;
            } else if (node instanceof SignalTransition) {
                switch (((SignalTransition) node).getSignalType()) {
                case INPUT: return Tag.INPUT;
                case OUTPUT: return Tag.OUTPUT;
                case INTERNAL: return Tag.INTERNAL;
                }
            }
        }
        return Tag.NONE;
    }

    private Tag getInputEventTag(WorkspaceEntry we, Trace componentTrace, String signal, SignalTransition.Direction direction) {
        StgModel cloneStg = WorkspaceUtils.getAs(WorkUtils.cloneModel(we.getModelEntry()), StgModel.class);
        if (PetriUtils.fireTrace(cloneStg, componentTrace)) {
            for (Transition t : PetriUtils.getEnabledTransitions(cloneStg)) {
                if (t instanceof SignalTransition) {
                    SignalTransition st = (SignalTransition) t;
                    if (signal.equals(st.getSignalName()) && (st.getDirection() == direction)) {
                        return Tag.INPUT;
                    }
                }
            }
            return Tag.VIOLATION;
        }
        return Tag.NONE;
    }

}
