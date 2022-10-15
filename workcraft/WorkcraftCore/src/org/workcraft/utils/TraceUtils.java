package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SimulationTool;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Pair;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.regex.Pattern;

public class TraceUtils {

    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String TRACE_SEPARATOR = "\n";
    private static final String POSITION_SEPARATOR = ":";
    private static final String ITEM_SEPARATOR = ",";
    private static final String LOOP_PREFIX = "{";
    private static final String LOOP_SUFFIX = "}";

    private static final String SELF_LOOP_DECORATION = (char) 0x2282 + " ";
    private static final String TO_LOOP_DECORATION = (char) 0x256D + " ";
    private static final String THROUGH_LOOP_DECORATION = (char) 0x254E + " ";
    private static final String FROM_LOOP_DECORATION = (char) 0x2570 + " ";

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
                result += TRACE_SEPARATOR;
                result += serialiseTrace(branchTrace);
            }
        }
        return result;
    }

    public static String serialiseTrace(Trace trace) {
        return serialisePosition(trace.getPosition()) + String.join(ITEM_SEPARATOR + SPACE, trace);
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
        if (!result.isEmpty()) result += ITEM_SEPARATOR + SPACE;
        result += LOOP_PREFIX + serialiseTrace(loopTrace) + LOOP_SUFFIX;
        return result;
    }

    private static String serialisePosition(int position) {
        return position > 0 ? position + POSITION_SEPARATOR + SPACE : EMPTY;
    }

    public static Solution deserialiseSolution(String str) {
        String[] parts = str.split(TRACE_SEPARATOR);
        Trace mainTrace = parts.length > 0 ? deserialiseTrace(parts[0]) : null;
        Trace branchTrace = parts.length > 1 ? deserialiseTrace(parts[1]) : null;
        Solution result = new Solution(mainTrace, branchTrace);
        if (parts.length > 0) {
            result.setLoopPosition(extractLoopPosition(parts[0]));
        }
        return result;
    }

    private static int extractLoopPosition(String str) {
        String[] parts = str.split(Pattern.quote(LOOP_PREFIX));
        if (parts.length > 1) {
            Trace trace = deserialiseTrace(parts[0]);
            return trace.size();
        }
        return -1;
    }

    public static Trace deserialiseTrace(String str) {
        Trace result = new Trace();
        String[] parts = str.replaceAll("\\s", EMPTY)
                .replaceAll(Pattern.quote(LOOP_PREFIX), EMPTY)
                .replaceAll(Pattern.quote(LOOP_SUFFIX), EMPTY)
                .split(Pattern.quote(POSITION_SEPARATOR));

        // Trace
        String refs = parts.length > 0 ? parts[parts.length - 1] : EMPTY;
        for (String ref : refs.split(Pattern.quote(ITEM_SEPARATOR))) {
            if (!ref.isEmpty()) {
                result.add(ref);
            }
        }

        // Position
        if (parts.length > 1) {
            try {
                int position = Integer.parseInt(parts[0]);
                result.setPosition(position);
            } catch (NumberFormatException  e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static void playSolution(WorkspaceEntry we, Solution solution, String suffix) {
        final Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            GraphEditor editor = framework.getMainWindow().getOrCreateEditor(we);
            Toolbox toolbox = editor.getToolBox();
            SimulationTool tool = toolbox.getToolInstance(SimulationTool.class);
            toolbox.selectTool(tool);

            Trace mainTrace = solution.getMainTrace();
            Trace branchTrace = solution.getBranchTrace();
            tool.setTraces(mainTrace, branchTrace, solution.getLoopPosition(), editor);

            String comment = solution.getComment();
            if ((comment != null) && !comment.isEmpty()) {
                String traceText = "\n" + mainTrace;
                if (branchTrace != null) {
                    traceText += "\n" + branchTrace;
                }
                // Remove HTML tags before printing the message
                String message = comment.replaceAll("<.*?>", "") + suffix + ": " + traceText;
                LogUtils.logWarning(message);
            }
        }
    }

    public static String addLoopDecoration(String ref, boolean isFirst, boolean isLast) {
        if (ref == null) {
            return null;
        }
        if (isFirst && isLast) {
            return SELF_LOOP_DECORATION + ref;
        }
        if (isFirst) {
            return TO_LOOP_DECORATION + ref;
        }
        if (isLast) {
            return FROM_LOOP_DECORATION + ref;
        }
        return THROUGH_LOOP_DECORATION + ref;
    }

    public static Pair<String, String> splitLoopDecoration(String str) {
        if (str != null) {
            if (str.startsWith(SELF_LOOP_DECORATION)) {
                return Pair.of(SELF_LOOP_DECORATION, str.substring(SELF_LOOP_DECORATION.length()));
            }
            if (str.startsWith(TO_LOOP_DECORATION)) {
                return Pair.of(TO_LOOP_DECORATION, str.substring(TO_LOOP_DECORATION.length()));
            }
            if (str.startsWith(THROUGH_LOOP_DECORATION)) {
                return Pair.of(THROUGH_LOOP_DECORATION, str.substring(THROUGH_LOOP_DECORATION.length()));
            }
            if (str.startsWith(FROM_LOOP_DECORATION)) {
                return Pair.of(FROM_LOOP_DECORATION, str.substring(FROM_LOOP_DECORATION.length()));
            }
        }
        return Pair.of(EMPTY, str);
    }

}
