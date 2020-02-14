package org.workcraft.gui.simulation;

import org.workcraft.Framework;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.tools.SimulationTool;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.List;

public class SimulationUtils {

    public static Trace getTrace(String str) {
        Trace result = new Trace();
        String[] parts = str.replaceAll("\\s", "").split(":");
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
                String traceText = solution.getMainTrace().toText();
                String message = comment.replaceAll("\\<.*?>", "") + " after trace: " + traceText;
                LogUtils.logWarning(message);
            }
        }
    }

}
