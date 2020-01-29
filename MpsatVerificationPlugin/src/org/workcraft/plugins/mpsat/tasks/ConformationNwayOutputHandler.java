package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.simulation.Solution;
import org.workcraft.gui.simulation.Trace;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.gui.simulation.ReachibilityDialog;
import org.workcraft.plugins.mpsat.utils.EnablednessUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.types.Triple;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

public class ConformationNwayOutputHandler extends ConformationOutputHandler {

    // Right arrow symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);

    private static final String TAG_INPUT = "i";
    private static final String TAG_OUTPUT = "o";
    private static final String TAG_INTERNAL = "x";
    private static final String TAG_DUMMY = "d";
    private static final String TAG_VIOLATION = "!";
    private static final String TAG_NONE = ".";

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

    ConformationNwayOutputHandler(ArrayList<WorkspaceEntry> wes, ExportOutput exportOutput,
            PcompOutput pcompOutput, VerificationOutput mpsatOutput, VerificationParameters settings) {

        super(wes.get(0), exportOutput, pcompOutput, mpsatOutput, settings);
        this.wes = wes;
    }

    @Override
    public Map<String, String> getSubstitutions(WorkspaceEntry we) {
        int index = wes.indexOf(we);
        return getSubstitutions(index);
    }

    @Override
    public ComponentData getCompositionData(WorkspaceEntry we) {
        int index = wes.indexOf(we);
        return getCompositionData(index);
    }

    @Override
    public StgModel getSrcStg(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        ModelEntry me = framework.cloneModel(we.getModelEntry());
        return WorkspaceUtils.getAs(me, StgModel.class);
    }

    @Override
    public void run() {
        List<Solution> solutions = getSolutions();
        boolean isConformant = solutions.isEmpty();
        String message = getMessage(!isConformant);
        if (isConformant) {
            DialogUtils.showInfo(message, TITLE);
        } else {
            LogUtils.logWarning(message);
            reportSolutions(solutions);
            Framework framework = Framework.getInstance();
            MainWindow mainWindow = framework.getMainWindow();
            for (WorkspaceEntry we : wes) {
                List<Solution> processedSolutions = processSolutions(we, solutions);
                if (!processedSolutions.isEmpty() && framework.isInGuiMode()) {
                    mainWindow.requestFocus(we);
                    String title = TITLE + " for model '" + we.getTitle() + "'";
                    String extendedMessage = extendMessage(message);
                    ReachibilityDialog solutionsDialog = new ReachibilityDialog(
                            mainWindow, we, title, extendedMessage, processedSolutions);

                    solutionsDialog.reveal();
                }
            }
        }
    }
    @Override
    public void reportSolutions(List<Solution> solutions) {
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.BRIEF) {
            super.reportSolutions(solutions);
        }
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.TABLE) {
            writeTables(solutions);
        }
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.LIST) {
            writeLists(solutions);
        }
    }

    private void writeTables(List<Solution> solutions) {
        String indent = String.join("", Collections.nCopies(2, " "));
        for (Solution solution : solutions) {
            writeTableHeader("Projected events", indent);
            writeTableBody(solution, indent);
        }
        writeTableLegend("");
    }

    private void writeTableHeader(String extraTitle, String indent) {
        String prefix = "";
        for (WorkspaceEntry we : wes) {
            String title = we.getTitle();
            LogUtils.logMessage(indent + prefix + title);
            prefix += "| ";
        }
        LogUtils.logMessage(indent + prefix + " " + extraTitle);
        int n = prefix.length() + extraTitle.length() + 1;
        String separator = String.join("", Collections.nCopies(n, "-"));
        LogUtils.logMessage(indent + separator);
    }

    private void writeTableBody(Solution solution, String indent) {
        Trace compTrace = solution.getMainTrace();
        Map<WorkspaceEntry, Trace> workToTraceMap = calcProjectionTraces(compTrace);
        String unexpectedEvent = findUnexpectedOutputAfterTrace(compTrace, workToTraceMap);
        Trace projectedEvents = calcProjectedEvents(compTrace, unexpectedEvent, workToTraceMap);
        Map<WorkspaceEntry, Trace> workToTagsMap = calcProjectionTags(workToTraceMap, unexpectedEvent);
        for (int i = 0; i < projectedEvents.size(); i++) {
            String line = indent;
            for (WorkspaceEntry we : wes) {
                Trace tags = workToTagsMap.get(we);
                line += (i < tags.size() ? tags.get(i) : TAG_NONE) + " ";
            }
            line += " " + projectedEvents.get(i);
            LogUtils.logMessage(line);
        }
    }

    private Map<WorkspaceEntry, Trace> calcProjectionTraces(Trace compTrace) {
        Map<WorkspaceEntry, Trace> result = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            Trace projTrace = calcProjectionTrace(compTrace, we);
            result.put(we, projTrace);
        }
        return result;
    }

    private Trace calcProjectionTrace(Trace compTrace, WorkspaceEntry we) {
        ComponentData data = getCompositionData(we);
        Map<String, String> substitutions = getSubstitutions(we);
        Trace result = new Trace();
        for (String ref : compTrace) {
            String srcRef = data.getSrcTransition(ref);
            if ((srcRef != null) && (substitutions.containsKey(srcRef))) {
                srcRef = substitutions.get(srcRef);
            }
            result.add(srcRef);
        }
        return result;
    }

    private String findUnexpectedOutputAfterTrace(Trace compTrace, Map<WorkspaceEntry, Trace> workToTraceMap) {
        // Find output enabled in component STG that is not enabled in the composition STG
        StgModel compStg = getMpsatOutput().getInputStg();
        Enabledness compEnabledness = EnablednessUtils.getOutputEnablednessAfterTrace(compStg, compTrace);
        for (WorkspaceEntry we : wes) {
            StgModel stg = getSrcStg(we);
            Trace projTrace = workToTraceMap.get(we);
            // Execute projected trace to a potentially problematic state
            if (!PetriUtils.fireTrace(stg, projTrace)) {
                throw new RuntimeException("Cannot execute projected trace: " + projTrace.toText());
            }
            // Find enabled signals whose state is unknown (due to dummies) in the composition STG.
            // If there is only one such signal, then it is actually the one disabled in the composition STG.
            HashSet<String> suspiciousSignals = EnablednessUtils.getEnabledSignals(stg, Signal.Type.OUTPUT);
            suspiciousSignals.retainAll(compEnabledness.getUnknownSet());
            if (suspiciousSignals.size() == 1) {
                compEnabledness.alter(Collections.emptySet(), suspiciousSignals, Collections.emptySet());
            }
            // Find the first enabled transition that is definitely disabled in composition STG.
            for (SignalTransition transition : stg.getSignalTransitions(Signal.Type.OUTPUT)) {
                String signalRef = stg.getSignalReference(transition);
                if (stg.isEnabled(transition) && compEnabledness.isDisabled(signalRef)) {
                    return stg.getNodeReference(transition);
                }
            }
        }
        return null;
    }

    private Trace calcProjectedEvents(Trace compTrace, String unexpectedEvent, Map<WorkspaceEntry, Trace> workToTraceMap) {
        Trace result = new Trace(compTrace);
        for (WorkspaceEntry we : wes) {
            StgModel stg = getSrcStg(we);
            Trace projTrace = workToTraceMap.get(we);
            int i = 0;
            for (String ref : projTrace) {
                if ((ref != null) && (i < result.size())) {
                    MathNode node = stg.getNodeByReference(ref);
                    boolean isNonInputTransition = node != null;
                    if (node instanceof SignalTransition) {
                        SignalTransition st = (SignalTransition) node;
                        isNonInputTransition = st.getSignalType() != Signal.Type.INPUT;
                    }
                    if (isNonInputTransition) {
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
            Trace projTrace = workToTraceMap.get(we);
            Trace tags = getTraceTags(projTrace, unexpectedEvent, we);
            workToTagsMap.put(we, tags);
        }
        return workToTagsMap;
    }

    private Trace getTraceTags(Trace projTrace, String unexpectedEvent, WorkspaceEntry we) {
        Trace result = new Trace();
        StgModel stg = getSrcStg(we);
        for (String ref : projTrace) {
            String tag = getNodeTag(stg, ref);
            result.add(tag);
        }
        if (unexpectedEvent != null) {
            Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(unexpectedEvent);
            if (r != null) {
                String signal = r.getFirst();
                SignalTransition.Direction direction = r.getSecond();
                Set<String> inputs = stg.getSignalReferences(Signal.Type.INPUT);
                Set<String> outputs = stg.getSignalReferences(Signal.Type.OUTPUT);
                String tag = TAG_NONE;
                if (outputs.contains(signal)) {
                    tag = TAG_OUTPUT;
                } else if (inputs.contains(signal) && PetriUtils.fireTrace(stg, projTrace)) {
                    boolean violation = true;
                    for (Transition t : PetriUtils.getEnabledTransitions(stg)) {
                        if (t instanceof SignalTransition) {
                            SignalTransition st = (SignalTransition) t;
                            if (signal.equals(st.getSignalName()) && (st.getDirection() == direction)) {
                                violation = false;
                            }
                        }
                    }
                    tag = violation ? TAG_VIOLATION : TAG_INPUT;
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
                return TAG_DUMMY;
            } else if (node instanceof SignalTransition) {
                switch (((SignalTransition) node).getSignalType()) {
                case INPUT: return TAG_INPUT;
                case OUTPUT: return TAG_OUTPUT;
                case INTERNAL: return TAG_INTERNAL;
                }
            }
        }
        return TAG_NONE;
    }

    private void writeTableLegend(String indent) {
        LogUtils.logMessage(indent + "Legend: "
                + TAG_INPUT + " - input; "
                + TAG_OUTPUT + " - output; "
                + TAG_INTERNAL + " - internal; "
                + TAG_DUMMY + " - dummy; "
                + TAG_VIOLATION + " - violation");
    }

    private void writeLists(List<Solution> solutions) {
        for (Solution solution : solutions) {
            writeList(solution, "  ");
        }
    }

    private void writeList(Solution solution, String indent) {
        Trace compTrace = solution.getMainTrace();
        Map<WorkspaceEntry, Trace> workToTraceMap = calcProjectionTraces(compTrace);
        String unexpectedEvent = findUnexpectedOutputAfterTrace(compTrace, workToTraceMap);
        Trace projectedEvents = calcProjectedEvents(compTrace, unexpectedEvent, workToTraceMap);
        Map<WorkspaceEntry, Trace> workToTagsMap = calcProjectionTags(workToTraceMap, unexpectedEvent);
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
                    if (TAG_OUTPUT.equals(tag)) {
                        outputRefs.add(title);
                    }
                    if (TAG_INPUT.equals(tag)) {
                        inputRefs.add(title);
                    }
                    if (TAG_INTERNAL.equals(tag) || TAG_DUMMY.equals(tag)) {
                        internalRefs.add(title);
                    }
                    if (TAG_VIOLATION.equals(tag)) {
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

}
