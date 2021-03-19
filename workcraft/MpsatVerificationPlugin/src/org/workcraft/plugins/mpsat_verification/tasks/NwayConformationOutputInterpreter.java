package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.dialogs.ReachabilityDialog;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.projection.Enabledness;
import org.workcraft.plugins.mpsat_verification.projection.ProjectionBuilder;
import org.workcraft.plugins.mpsat_verification.projection.ProjectionEvent;
import org.workcraft.plugins.mpsat_verification.projection.ProjectionTrace;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.mpsat_verification.utils.OutcomeUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

public class NwayConformationOutputInterpreter extends ConformationOutputInterpreter {

    // Right arrow symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);

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

    public NwayConformationOutputInterpreter(ArrayList<WorkspaceEntry> wes, ExportOutput exportOutput,
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

    public void logSolutions(List<Solution> solutions) {
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.BRIEF) {
            writeBrief(solutions);
        }
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.TABLE) {
            writeTables(solutions);
        }
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.LIST) {
            writeLists(solutions);
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

                ProjectionBuilder projectionBuilder = new ProjectionBuilder(compositionSolution, getCompositionData(), wes);
                Set<String> unexpectedlyEnabledOutputEvents = projectionBuilder.getUnexpectedlyEnabledOutputEvents(we);
                Enabledness componentEnabledness = projectionBuilder.getComponentEnabledness(we);
                result.addAll(CompositionUtils.getEnabledViolatorSolutions(componentTrace,
                        unexpectedlyEnabledOutputEvents, componentEnabledness));
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
        ProjectionBuilder projectionBuilder = new ProjectionBuilder(solution, getCompositionData(), wes);
        Trace compositionViolationTrace = projectionBuilder.getCompositionTraceWithViolationEvent();
        Map<WorkspaceEntry, ProjectionTrace> componentProjectionTraceMap = projectionBuilder.calcComponentProjectionTraces();

        for (int i = 0; i < compositionViolationTrace.size(); i++) {
            StringBuilder line = new StringBuilder(indent);
            for (WorkspaceEntry we : wes) {
                ProjectionTrace projectionTrace = componentProjectionTraceMap.get(we);
                ProjectionEvent.Tag tag = i < projectionTrace.size()
                        ? projectionTrace.get(i).tag
                        : ProjectionEvent.Tag.NONE;

                line.append(convertTagToString(tag)).append(" ");
            }
            line.append(" ").append(compositionViolationTrace.get(i));
            LogUtils.logMessage(line.toString());
        }
    }

    private void writeTableLegend(String indent) {
        StringBuilder msg = new StringBuilder();
        for (ProjectionEvent.Tag tag : ProjectionEvent.Tag.values()) {
            String tagLabel = convertTagToString(tag);
            String tagDescription = tag.toString();
            if ((tagLabel != null) && (tagDescription != null)) {
                if (msg.length() > 0) {
                    msg.append("; ");
                }
                msg.append(tagLabel).append(" - ").append(tagDescription);
            }
        }
        if (msg.length() > 0) {
            LogUtils.logMessage(indent + "Legend: " + msg);
        }
    }

    private void writeLists(List<Solution> solutions) {
        for (Solution solution : solutions) {
            writeList(solution, "  ");
        }
    }

    private void writeList(Solution solution, String indent) {
        ProjectionBuilder projectionBuilder = new ProjectionBuilder(solution, getCompositionData(), wes);
        Trace violationTrace = projectionBuilder.getCompositionTraceWithViolationEvent();
        Map<WorkspaceEntry, ProjectionTrace> componentProjectionTraceMap = projectionBuilder.calcComponentProjectionTraces();

        int maxLen = violationTrace.stream().mapToInt(String::length).max().orElse(0);
        for (int i = 0; i < violationTrace.size(); i++) {
            List<String> inputRefs = new ArrayList<>();
            List<String> outputRefs = new ArrayList<>();
            List<String> internalRefs = new ArrayList<>();
            for (WorkspaceEntry we : wes) {
                ProjectionTrace projectionTrace = componentProjectionTraceMap.get(we);
                String title = we.getTitle();
                if (i < projectionTrace.size()) {
                    ProjectionEvent projectionEvent = projectionTrace.get(i);
                    if (projectionEvent.tag == ProjectionEvent.Tag.OUTPUT) {
                        outputRefs.add(title);
                    }
                    if (projectionEvent.tag == ProjectionEvent.Tag.INPUT) {
                        inputRefs.add(title);
                    }
                    if ((projectionEvent.tag == ProjectionEvent.Tag.INTERNAL)
                            || (projectionEvent.tag == ProjectionEvent.Tag.DUMMY)) {

                        internalRefs.add(title);
                    }
                    if (projectionEvent.tag == ProjectionEvent.Tag.VIOLATION) {
                        inputRefs.add(title + " (unexpected)");
                    }
                }

            }
            String inputStr = String.join(", ", inputRefs);
            String outputStr = String.join(", ", outputRefs);
            String internalStr = String.join(", ", internalRefs);
            String prefix = String.format(indent + "%-" + maxLen + "s : ", violationTrace.get(i));
            if (!internalStr.isEmpty()) {
                LogUtils.logMessage(prefix + internalStr);
            } else {
                LogUtils.logMessage(prefix + outputStr + " " + RIGHT_ARROW_SYMBOL + " " + inputStr);
            }
        }
    }

    private String convertTagToString(ProjectionEvent.Tag tag) {
        switch (tag) {
        case INPUT: return "i";
        case OUTPUT: return "o";
        case INTERNAL: return "x";
        case DUMMY: return "d";
        case VIOLATION: return "!";
        case NONE: return ".";
        }
        return null;
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
            logSolutions(solutions);
            reportSolutions(extendMessage(message), solutions);
        }
        return propertyHolds;
    }

}
