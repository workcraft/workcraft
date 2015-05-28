package org.workcraft.plugins.son.tools;

import org.workcraft.Tool;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.plugins.son.SON;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeConsistencyChecker implements Tool{

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);
	}

	@Override
	public String getSection(){
		return "Time analysis";
	}

	@Override
	public String getDisplayName() {
		return "Consistency";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final ToolboxPanel toolbox = ToolManager.getToolboxPanel(we);
		final TimeAnalysisTool tool = toolbox.getToolInstance(TimeAnalysisTool.class);
		toolbox.selectTool(tool);
	}

}
