package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.dom.references.Identifier;
import org.workcraft.plugins.mpsat_verification.projection.ProjectionBuilder;
import org.workcraft.plugins.mpsat_verification.projection.ProjectionEvent;
import org.workcraft.plugins.mpsat_verification.projection.ProjectionSlice;
import org.workcraft.plugins.mpsat_verification.projection.ProjectionTrace;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Pair;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.workcraft.plugins.mpsat_verification.projection.ProjectionEvent.Tag.VIOLATION;

public class CompositionSolutionLogger {

    // Right arrow symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    private static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);

    public enum Style {
        BRIEF("Brief"),
        TABLE("Table"),
        LIST("List");

        private final String name;

        Style(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final List<WorkspaceEntry> wes;
    private final CompositionData compositionData;
    private final boolean useWorkHierarchy;

    public CompositionSolutionLogger(List<WorkspaceEntry> wes, CompositionData compositionData, boolean useWorkHierarchy) {
        this.wes = wes;
        this.compositionData = compositionData;
        this.useWorkHierarchy = useWorkHierarchy;
    }

    public void write(List<Solution> solutions, Style style) {
        if (style == Style.BRIEF) {
            writeBrief(solutions);
        }
        if (style == Style.TABLE) {
            writeTables(solutions);
        }
        if (style == Style.LIST) {
            writeLists(solutions);
        }
    }

    private void writeBrief(List<Solution> solutions) {
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Violation traces of the composition:");
        }
        for (Solution solution : solutions) {
            Trace trace = solution.getMainTrace();
            if (needsMultiLineMessage) {
                LogUtils.logMessage("  " + trace);
            } else {
                LogUtils.logMessage("Violation trace of the composition: " + trace);
            }
        }
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
        LogUtils.logMessage(indent + prefix + ' ' + extraTitle);
        int n = prefix.length() + extraTitle.length() + 1;
        String separator = TextUtils.repeat("-", n);
        LogUtils.logMessage(indent + separator);
    }

    private void writeTableBody(Solution solution, String indent) {
        ProjectionBuilder projectionBuilder = new ProjectionBuilder(solution, compositionData, wes);
        Trace compositionViolationTrace = projectionBuilder.getCompositionTraceWithViolationEvent();
        Map<WorkspaceEntry, ProjectionTrace> componentProjectionTraceMap =
                projectionBuilder.calcComponentProjectionTraces();

        for (int i = 0; i < compositionViolationTrace.size(); i++) {
            StringBuilder line = new StringBuilder(indent);
            ProjectionSlice projectionSlice = new ProjectionSlice();
            for (WorkspaceEntry we : wes) {
                ProjectionTrace projectionTrace = componentProjectionTraceMap.get(we);
                ProjectionEvent projectedEvent = i < projectionTrace.size() ? projectionTrace.get(i)
                        : new ProjectionEvent(ProjectionEvent.Tag.NONE, null);

                projectionSlice.put(we, projectedEvent);
                line.append(convertTagToString(projectedEvent.tag)).append(' ');
            }
            String suggestedEvent = getSuggestedEventRef(projectionSlice);
            if (suggestedEvent != null) {
                line.append(' ').append(suggestedEvent);
            }
            LogUtils.logMessage(line.toString());
        }
    }

    private void writeTableLegend(String indent) {
        StringBuilder msg = new StringBuilder();
        for (ProjectionEvent.Tag tag : ProjectionEvent.Tag.values()) {
            String tagLabel = convertTagToString(tag);
            String tagDescription = tag.toString();
            if (tagDescription != null) {
                if (!msg.isEmpty()) {
                    msg.append("; ");
                }
                msg.append(tagLabel).append(" - ").append(tagDescription);
            }
        }
        if (!msg.isEmpty()) {
            LogUtils.logMessage(indent + "Legend: " + msg);
        }
    }

    private void writeLists(List<Solution> solutions) {
        for (Solution solution : solutions) {
            writeList(solution, "  ");
        }
    }

    private void writeList(Solution solution, String indent) {
        ProjectionBuilder projectionBuilder = new ProjectionBuilder(solution, compositionData, wes);
        Trace compositionTrace = projectionBuilder.getCompositionTraceWithViolationEvent();
        Map<WorkspaceEntry, ProjectionTrace> componentProjectionTraceMap = projectionBuilder.calcComponentProjectionTraces();

        List<Pair<String, String>> partitionedLines = new ArrayList<>();
        for (int i = 0; i < compositionTrace.size(); i++) {
            List<String> inputWorkTitles = new ArrayList<>();
            List<String> outputWorkTitles = new ArrayList<>();
            List<String> internalWorkTitles = new ArrayList<>();
            ProjectionSlice projectionSlice = new ProjectionSlice();
            for (WorkspaceEntry we : wes) {
                ProjectionTrace projectionTrace = componentProjectionTraceMap.get(we);
                String title = we.getTitle();
                if (i < projectionTrace.size()) {
                    ProjectionEvent projectionEvent = projectionTrace.get(i);
                    projectionSlice.put(we, projectionEvent);
                    if (projectionEvent.tag == ProjectionEvent.Tag.OUTPUT) {
                        outputWorkTitles.add(title);
                    }
                    if (projectionEvent.tag == ProjectionEvent.Tag.INPUT) {
                        inputWorkTitles.add(title);
                    }
                    if ((projectionEvent.tag == ProjectionEvent.Tag.INTERNAL)
                            || (projectionEvent.tag == ProjectionEvent.Tag.DUMMY)) {

                        internalWorkTitles.add(title);
                    }
                    if (projectionEvent.tag == VIOLATION) {
                        inputWorkTitles.add(title + " (unexpected)");
                    }
                }
            }
            String prefix = getSuggestedEventRef(projectionSlice);
            String suffix  = !internalWorkTitles.isEmpty() ? String.join(", ", internalWorkTitles)
                    : (String.join(", ", outputWorkTitles) + ' ' + RIGHT_ARROW_SYMBOL
                        + " " + String.join(", ", inputWorkTitles));

            partitionedLines.add(Pair.of(prefix, suffix));
        }

        int maxLength = partitionedLines.stream().mapToInt(line -> line.getFirst().length()).max().orElse(0);
        for (Pair<String, String> line : partitionedLines) {
            String paddedPrefix = String.format("%-" + maxLength + "s", line.getFirst());
            LogUtils.logMessage(indent + paddedPrefix + " : " + line.getSecond());
        }
    }

    private String getSuggestedEventRef(ProjectionSlice projectionSlice) {
        String badEventRef = null;
        String outputEventRef = null;
        String inputEventRef = null;
        String internalEventRef = null;
        String dummyEventRef = null;
        for (Map.Entry<WorkspaceEntry, ProjectionEvent> projectionEntry : projectionSlice.entrySet()) {
            ProjectionEvent projectionEvent = projectionEntry.getValue();
            if (projectionEvent != null) {
                WorkspaceEntry we = projectionEntry.getKey();
                String projectionEventRef = getProjectedEventRef(projectionEvent, we);
                switch (projectionEvent.tag) {
                    case VIOLATION -> badEventRef = (badEventRef == null) ? projectionEventRef : badEventRef;
                    case OUTPUT -> outputEventRef = (outputEventRef == null) ? projectionEventRef : outputEventRef;
                    case INTERNAL -> internalEventRef = projectionEventRef;
                    case DUMMY -> dummyEventRef = projectionEventRef;
                    case INPUT -> inputEventRef = projectionEventRef;
                    default -> { }
                }
            }
        }

        return badEventRef != null ? badEventRef
                : outputEventRef != null ? outputEventRef
                : internalEventRef != null ? internalEventRef
                : dummyEventRef != null ? dummyEventRef
                : inputEventRef;
    }

    private String getProjectedEventRef(ProjectionEvent projectionEvent, WorkspaceEntry we) {
        if (!useWorkHierarchy) {
            return projectionEvent.ref;
        }
        String title = we.getTitle();
        String hierarchPrefix = (title == null) ? "" : Identifier.appendNamespaceSeparator(title);
        return hierarchPrefix + projectionEvent.ref;
    }

    private String convertTagToString(ProjectionEvent.Tag tag) {
        return switch (tag) {
            case INPUT -> "i";
            case OUTPUT -> "o";
            case INTERNAL -> "x";
            case DUMMY -> "d";
            case VIOLATION -> "!";
            case NONE -> ".";
        };
    }

}
