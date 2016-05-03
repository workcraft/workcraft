package org.workcraft.plugins.cpog.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.workspace.WorkspaceEntry;

public class AlgebraExpressionFromGraphsTool implements Tool {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		if (we.getModelEntry() == null) return false;
        if (we.getModelEntry().getVisualModel() instanceof VisualCPOG) return true;
		return false;
	}

	@Override
	public String getSection() {
		return "! Algebra";
	}

	@Override
	public String getDisplayName() {
		return "Get expression from graphs (selected or all)";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final Framework framework = Framework.getInstance();
        final GraphEditorPanel editor = framework.getMainWindow().getCurrentEditor();
        final ToolboxPanel toolbox = editor.getToolBox();
        final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);
        
		VisualCPOG visualCpog = (VisualCPOG) editor.getWorkspaceEntry().getModelEntry().getVisualModel();
		
		String exp = tool.getParsingTool().getExpressionFromGraph(visualCpog);
		
		if (exp != "") {
            tool.setExpressionText(exp);
		}
	}

}
