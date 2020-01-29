package org.workcraft.gui.simulation;

import org.workcraft.Framework;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.tools.SimulationTool;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.List;

public class SimulationUtils {

    public static Trace getTrace(String text) {
        Trace trace = null;
        if (text != null) {
            trace = new Trace();
            String[] refs = text.replaceAll("\\s", "").split(",");
            for (String ref : refs) {
                String transition = ref.substring(ref.indexOf(':') + 1);
                if (!transition.isEmpty()) {
                    trace.add(transition);
                }
            }
        }
        return trace;
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
        return (solution != null) && ((solution.getMainTrace() != null) || (solution.getBranchTrace() != null));
    }

    public static void playSolution(WorkspaceEntry we, Solution solution) {
        final Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            GraphEditorPanel editor = framework.getMainWindow().getEditor(we);
            final Toolbox toolbox = editor.getToolBox();
            final SimulationTool tool = toolbox.getToolInstance(SimulationTool.class);
            toolbox.selectTool(tool);
            tool.setTrace(solution.getMainTrace(), solution.getBranchTrace(), editor);
            String comment = solution.getComment();
            if ((comment != null) && !comment.isEmpty()) {
                String traceText = solution.getMainTrace().toText();
                String message = comment.replaceAll("\\<.*?>", "") + " after trace: " + traceText;
                LogUtils.logWarning(message);
            }
        }
    }

}
