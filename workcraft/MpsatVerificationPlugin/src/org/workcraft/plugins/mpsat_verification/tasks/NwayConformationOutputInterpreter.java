package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.dialogs.ReachabilityDialog;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.mpsat_verification.utils.OutcomeUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Triple;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

public class NwayConformationOutputInterpreter extends ConformationOutputInterpreter {

    // Right arrow symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);

    private static final String INPUT_TAG = "i";
    private static final String OUTPUT_TAG = "o";
    private static final String INTERNAL_TAG = "x";
    private static final String DUMMY_TAG = "d";
    private static final String VIOLATION_TAG = "!";
    private static final String NONE_TAG = ".";

    public enum ConformationReportStyle {
        BRIEF("Brief"),
        TABLE("Table"),
        LIST("List");

        private final String name;

        ConformationReportStyle(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final List<WorkspaceEntry> wes;

    NwayConformationOutputInterpreter(ArrayList<WorkspaceEntry> wes, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput,  boolean interactive) {

        super(wes.get(0), exportOutput, pcompOutput, mpsatOutput, interactive);
        this.wes = wes;
    }

    private ComponentData getComponentData(WorkspaceEntry we) {
        CompositionData compositionData = getCompositionData();
        return compositionData == null ? null : compositionData.getComponentData(wes.indexOf(we));
    }

    @Override
    public void reportSolutions(String message, List<Solution> solutions) {
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.BRIEF) {
            writeBrief(solutions);
        }
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.TABLE) {
            writeTables(solutions);
        }
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.LIST) {
            writeLists(solutions);
        }
        Framework framework = Framework.getInstance();
        if (isInteractive() && framework.isInGuiMode()) {
            MainWindow mainWindow = framework.getMainWindow();
            for (WorkspaceEntry we : wes) {
                List<Solution> processedSolutions = processSolutions(solutions, we);
                if (!processedSolutions.isEmpty() && framework.isInGuiMode()) {
                    mainWindow.requestFocus(we);
                    String title = OutcomeUtils.TITLE + " for model '" + we.getTitle() + "'";
                    ReachabilityDialog solutionsDialog = new ReachabilityDialog(
                            mainWindow, we, title, message, processedSolutions);

                    solutionsDialog.reveal();
                }
            }
        }
    }

    public List<Solution> processSolutions(List<Solution> solutions, WorkspaceEntry we) {
        String title = we.getTitle();
        StgModel compositionStg = getCompositionStg();
        ComponentData componentData = getComponentData(we);

        HashSet<String> visitedTraces = new HashSet<>();
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Unique projection(s) to '" + title + "':");
        }

        List<Solution> result = new LinkedList<>();
        for (Solution solution : solutions) {
            // Get unique projection trace
            Solution compositionSolution = MpsatUtils.fixSolutionToggleEvents(compositionStg, solution);
            Trace componentTrace = CompositionUtils.projectTrace(compositionSolution.getMainTrace(), componentData);
            String traceText = componentTrace.toString();
            if (!visitedTraces.contains(traceText)) {
                visitedTraces.add(traceText);
                if (needsMultiLineMessage) {
                    LogUtils.logMessage("  " + traceText);
                } else {
                    LogUtils.logMessage("Projection to '" + title + "': " + traceText);
                }

                Map<WorkspaceEntry, Map<String, Trace>> workEnablednessMap = calcEnablednessMap(compositionSolution);
                Set<String> disabledInputEvents = getDisabledInputEvents(workEnablednessMap);

                Map<String, Trace> componentEnabledness = workEnablednessMap.get(we);
                StgModel componentStg = WorkspaceUtils.getAs(we, StgModel.class);
                Set<String> outputSignals = componentStg.getSignalReferences(Signal.Type.OUTPUT);

                Set<String> unexpectedlyEnabledOutputEvents = StgUtils.getAllEvents(outputSignals);
                unexpectedlyEnabledOutputEvents.retainAll(componentEnabledness.keySet());
                unexpectedlyEnabledOutputEvents.retainAll(disabledInputEvents);
                result.addAll(CompositionUtils.extendTraceToViolations(componentTrace, componentEnabledness,
                        unexpectedlyEnabledOutputEvents, "Unexpected enabling of signal"));
            }
        }
        return result;
    }

    private void writeTables(List<Solution> solutions) {
        String indent = TextUtils.repeat(" ", 2);
        for (Solution solution : solutions) {
            writeTableHeader("Projected events", indent);
            writeTableBody(solution, indent);
        }
        writeTableLegend("");
    }

    private void writeTableHeader(String extraTitle, String indent) {
        StringBuilder prefix = new StringBuilder();
        for (WorkspaceEntry we : wes) {
            String title = we.getTitle();
            LogUtils.logMessage(indent + prefix + title);
            prefix.append("| ");
        }
        LogUtils.logMessage(indent + prefix + " " + extraTitle);
        int n = prefix.length() + extraTitle.length() + 1;
        String separator = TextUtils.repeat("-", n);
        LogUtils.logMessage(indent + separator);
    }

    private void writeTableBody(Solution solution, String indent) {
        Map<String, Trace> violationEnabledness = findViolationEnabledness(solution);
        String violationEvent = violationEnabledness.isEmpty() ? null : violationEnabledness.keySet().iterator().next();
        Trace violationContinuation = violationEnabledness.getOrDefault(violationEvent, new Trace());
        Trace compositionTrace = new Trace(solution.getMainTrace());
        compositionTrace.addAll(violationContinuation);

        Map<WorkspaceEntry, Trace> workToTraceMap = calcProjectedTraces(compositionTrace);
        Trace projectedEvents = calcProjectedEvents(compositionTrace, violationEvent, workToTraceMap);
        Map<WorkspaceEntry, Trace> workToTagsMap = calcProjectionTags(workToTraceMap, violationEvent);
        for (int i = 0; i < projectedEvents.size(); i++) {
            StringBuilder line = new StringBuilder(indent);
            for (WorkspaceEntry we : wes) {
                Trace tags = workToTagsMap.get(we);
                line.append(i < tags.size() ? tags.get(i) : NONE_TAG).append(" ");
            }
            line.append(" ").append(projectedEvents.get(i));
            LogUtils.logMessage(line.toString());
        }
    }

    private Map<String, Trace> findViolationEnabledness(Solution compositionSolution) {
        Map<String, Trace> result = new HashMap<>();
        Map<WorkspaceEntry, Map<String, Trace>> workEnablednessMap = calcEnablednessMap(compositionSolution);
        Set<String> disabledInputEvents = getDisabledInputEvents(workEnablednessMap);
        for (WorkspaceEntry we : wes) {
            Map<String, Trace> enabledness = workEnablednessMap.get(we);
            StgModel stg = WorkspaceUtils.getAs(we, StgModel.class);
            Set<String> outputSignals = stg.getSignalReferences(Signal.Type.OUTPUT);
            Set<String> violationOutputEvents = StgUtils.getAllEvents(outputSignals);
            violationOutputEvents.retainAll(enabledness.keySet());
            violationOutputEvents.retainAll(disabledInputEvents);

            for (String violationOutputEvent : violationOutputEvents) {
                Trace trace = enabledness.get(violationOutputEvent);
                result.put(violationOutputEvent, trace);
            }
        }
        return result;
    }

    private Set<String> getDisabledInputEvents(Map<WorkspaceEntry, Map<String, Trace>> workEnablednessMap) {
        Set<String> result = new HashSet<>();
        for (WorkspaceEntry we : wes) {
            Map<String, Trace> enabledness = workEnablednessMap.get(we);

            StgModel stg = WorkspaceUtils.getAs(we, StgModel.class);
            Set<String> inputSignals = stg.getSignalReferences(Signal.Type.INPUT);
            Set<String> inputEvents = StgUtils.getAllEvents(inputSignals);
            inputEvents.removeAll(enabledness.keySet());

            result.addAll(inputEvents);
        }
        return result;
    }

    private Map<WorkspaceEntry, Map<String, Trace>> calcEnablednessMap(Solution compositionSolution) {
        Map<WorkspaceEntry, Map<String, Trace>> result = new HashMap<>();
        Set<Trace> compositionContinuations = compositionSolution.getContinuations();
        for (WorkspaceEntry we : wes) {
            ComponentData componentData = getComponentData(we);
            Map<String, Trace> enabledness = CompositionUtils.getEnabledness(compositionContinuations, componentData);
            result.put(we, enabledness);
        }
        return result;
    }

    private Map<WorkspaceEntry, Trace> calcProjectedTraces(Trace compositionTrace) {
        Map<WorkspaceEntry, Trace> result = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            ComponentData componentData = getComponentData(we);
            Trace projectedTrace = new Trace();
            for (String ref : compositionTrace) {
                String srcRef = componentData == null ? null : componentData.getSrcTransition(ref);
                projectedTrace.add(srcRef);
            }
            result.put(we, projectedTrace);
        }
        return result;
    }

    private Trace calcProjectedEvents(Trace compositionTrace, String unexpectedEvent,
            Map<WorkspaceEntry, Trace> workToTraceMap) {

        Trace result = new Trace(compositionTrace);
        for (WorkspaceEntry we : wes) {
            StgModel componentStg = WorkspaceUtils.getAs(we, StgModel.class);
            Trace componentTrace = workToTraceMap.get(we);
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
        if (unexpectedEvent != null) {
            result.add(unexpectedEvent);
        }
        return result;
    }

    private Map<WorkspaceEntry, Trace> calcProjectionTags(Map<WorkspaceEntry, Trace> workToTraceMap, String unexpectedEvent) {
        Map<WorkspaceEntry, Trace> workToTagsMap = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            Trace componentTrace = workToTraceMap.get(we);
            Trace tags = getTraceTags(we, componentTrace, unexpectedEvent);
            workToTagsMap.put(we, tags);
        }
        return workToTagsMap;
    }

    private Trace getTraceTags(WorkspaceEntry we, Trace componentTrace, String unexpectedEvent) {
        Trace result = new Trace();
        StgModel stg = WorkspaceUtils.getAs(we, StgModel.class);
        for (String ref : componentTrace) {
            String tag = getNodeTag(stg, ref);
            result.add(tag);
        }
        if (unexpectedEvent != null) {
            Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(unexpectedEvent);
            if (r != null) {
                String signal = r.getFirst();
                Set<String> outputs = stg.getSignalReferences(Signal.Type.OUTPUT);
                String tag = NONE_TAG;
                if (outputs.contains(signal)) {
                    tag = OUTPUT_TAG;
                } else {
                    Set<String> inputs = stg.getSignalReferences(Signal.Type.INPUT);
                    if (inputs.contains(signal)) {
                        SignalTransition.Direction direction = r.getSecond();
                        tag = getInputEventTag(we, componentTrace, signal, direction);
                    }
                }
                result.add(tag);
            }
        }
        return result;
    }

    private String getNodeTag(StgModel stg, String ref) {
        if (ref != null) {
            MathNode node = stg.getNodeByReference(ref);
            if (node instanceof DummyTransition) {
                return DUMMY_TAG;
            } else if (node instanceof SignalTransition) {
                switch (((SignalTransition) node).getSignalType()) {
                case INPUT: return INPUT_TAG;
                case OUTPUT: return OUTPUT_TAG;
                case INTERNAL: return INTERNAL_TAG;
                }
            }
        }
        return NONE_TAG;
    }

    private String getInputEventTag(WorkspaceEntry we, Trace componentTrace, String signal, SignalTransition.Direction direction) {
        StgModel cloneStg = WorkspaceUtils.getAs(WorkUtils.cloneModel(we.getModelEntry()), StgModel.class);
        if (PetriUtils.fireTrace(cloneStg, componentTrace)) {
            for (Transition t : PetriUtils.getEnabledTransitions(cloneStg)) {
                if (t instanceof SignalTransition) {
                    SignalTransition st = (SignalTransition) t;
                    if (signal.equals(st.getSignalName()) && (st.getDirection() == direction)) {
                        return INPUT_TAG;
                    }
                }
            }
            return VIOLATION_TAG;
        }
        return NONE_TAG;
    }

    private void writeTableLegend(String indent) {
        LogUtils.logMessage(indent + "Legend: "
                + INPUT_TAG + " - input; "
                + OUTPUT_TAG + " - output; "
                + INTERNAL_TAG + " - internal; "
                + DUMMY_TAG + " - dummy; "
                + VIOLATION_TAG + " - violation");
    }

    private void writeLists(List<Solution> solutions) {
        for (Solution solution : solutions) {
            writeList(solution, "  ");
        }
    }

    private void writeList(Solution solution, String indent) {
        Map<String, Trace> violationEnabledness = findViolationEnabledness(solution);
        String violationEvent = violationEnabledness.isEmpty() ? null : violationEnabledness.keySet().iterator().next();
        Trace violationContinuation = violationEnabledness.getOrDefault(violationEvent, new Trace());

        Trace compositionTrace = new Trace(solution.getMainTrace());
        compositionTrace.addAll(violationContinuation);

        Map<WorkspaceEntry, Trace> workToTraceMap = calcProjectedTraces(compositionTrace);
        Trace projectedEvents = calcProjectedEvents(compositionTrace, violationEvent, workToTraceMap);
        Map<WorkspaceEntry, Trace> workToTagsMap = calcProjectionTags(workToTraceMap, violationEvent);
        int maxLen = projectedEvents.stream().mapToInt(String::length).max().orElse(0);
        for (int i = 0; i < projectedEvents.size(); i++) {
            List<String> inputRefs = new ArrayList<>();
            List<String> outputRefs = new ArrayList<>();
            List<String> internalRefs = new ArrayList<>();
            for (WorkspaceEntry we : wes) {
                Trace tags = workToTagsMap.get(we);
                String title = we.getTitle();
                if (i < tags.size()) {
                    String tag = tags.get(i);
                    if (OUTPUT_TAG.equals(tag)) {
                        outputRefs.add(title);
                    }
                    if (INPUT_TAG.equals(tag)) {
                        inputRefs.add(title);
                    }
                    if (INTERNAL_TAG.equals(tag) || DUMMY_TAG.equals(tag)) {
                        internalRefs.add(title);
                    }
                    if (VIOLATION_TAG.equals(tag)) {
                        inputRefs.add(title + " (unexpected)");
                    }
                }

            }
            String inputStr = String.join(", ", inputRefs);
            String outputStr = String.join(", ", outputRefs);
            String internalStr = String.join(", ", internalRefs);
            String prefix = String.format(indent + "%-" + maxLen + "s : ", projectedEvents.get(i));
            if (!internalStr.isEmpty()) {
                LogUtils.logMessage(prefix + internalStr);
            } else {
                LogUtils.logMessage(prefix + outputStr + " " + RIGHT_ARROW_SYMBOL + " " + inputStr);
            }
        }
    }

    @Override
    public Boolean interpret() {
        if (getOutput() == null) {
            return null;
        }
        List<Solution> solutions = getOutput().getSolutions();
        boolean propertyHolds = solutions.isEmpty();
        String message = getMessage(propertyHolds);
        if (propertyHolds) {
            OutcomeUtils.showOutcome(true, message, isInteractive());
        } else {
            OutcomeUtils.logOutcome(false, message);
            reportSolutions(extendMessage(message), solutions);
        }
        return propertyHolds;
    }

}
