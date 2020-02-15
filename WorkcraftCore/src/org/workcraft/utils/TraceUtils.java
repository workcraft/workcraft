package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.tools.SimulationTool;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.List;

public class TraceUtils {

    public static final String EMPTY_TEXT = "[empty]";

    public static String serialiseSolution(Solution solution) {
        String result = null;
        Trace mainTrace = solution.getMainTrace();
        if (mainTrace != null) {
            result = "";
            if (!solution.hasLoop()) {
                result += serialiseTrace(mainTrace);
            } else {
                result += serialisePrefixAndLoop(mainTrace, solution.getLoopPosition());
            }
            Trace branchTrace = solution.getBranchTrace();
            if (branchTrace != null) {
                result += "\n";
                result += serialiseTrace(branchTrace);
            }
        }
        return result;
    }

    public static String serialiseTrace(Trace trace) {
        return serialisePosition(trace.getPosition()) + String.join(", ", trace);
    }

    public static String serialisePrefixAndLoop(Trace trace, int loopPosition) {
        String result = serialisePosition(trace.getPosition());
        // Prefix
        Trace prefixTrace = new Trace();
        prefixTrace.addAll(trace.subList(0, loopPosition));
        result += serialiseTrace(prefixTrace);
        // Loop
        Trace loopTrace = new Trace();
        loopTrace.addAll(trace.subList(loopPosition, trace.size()));
        if (!result.isEmpty()) result += ", ";
        result += "(" + serialiseTrace(loopTrace) + ")*";
        return result;
    }

    private static String serialisePosition(int position) {
        return position > 0 ? position + ": " : "";
    }

    public static Solution deserialiseSolution(String str) {
        String[] parts = str.split("\n");
        Trace mainTrace = parts.length > 0 ? deserialiseTrace(parts[0]) : null;
        Trace branchTrace = parts.length > 1 ? deserialiseTrace(parts[1]) : null;
        Solution result = new Solution(mainTrace, branchTrace);
        if (parts.length > 0) {
            result.setLoopPosition(extractLoopPosition(parts[0]));
        }
        return result;
    }

    private static int extractLoopPosition(String str) {
        String[] parts = str.split("\\(");
        if (parts.length > 1) {
            Trace trace = deserialiseTrace(parts[0]);
            return trace.size();
        }
        return -1;
    }

    public static Trace deserialiseTrace(String str) {
        Trace result = new Trace();
        String[] parts = str.replaceAll("[\\s\\(\\)\\*]", "").split(":");
        // Trace
        String refs = parts.length > 0 ? parts[parts.length - 1] : "";
        for (String ref : refs.split(",")) {
            if (!ref.isEmpty()) {
                result.add(ref);
            }
        }
        // Position
        if (parts.length > 1) {
            try {
                int position = Integer.valueOf(parts[0]);
                result.setPosition(position);
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static boolean hasTraces(List<Solution> solutions) {
        if (solutions != null) {
            for (Solution solution : solutions) {
                if (hasTraces(solution)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasTraces(Solution solution) {
        return (solution != null) && solution.hasTrace();
    }

    public static void playSolution(WorkspaceEntry we, Solution solution) {
        final Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            GraphEditorPanel editor = framework.getMainWindow().getEditor(we);
            final Toolbox toolbox = editor.getToolBox();
            final SimulationTool tool = toolbox.getToolInstance(SimulationTool.class);
            toolbox.selectTool(tool);
            tool.setTraces(solution.getMainTrace(), solution.getBranchTrace(), solution.getLoopPosition(), editor);
            String comment = solution.getComment();
            if ((comment != null) && !comment.isEmpty()) {
                String traceText = solution.getMainTrace().toString();
                String message = comment.replaceAll("\\<.*?>", "") + " after trace: " + traceText;
                LogUtils.logWarning(message);
            }
        }
    }

}
