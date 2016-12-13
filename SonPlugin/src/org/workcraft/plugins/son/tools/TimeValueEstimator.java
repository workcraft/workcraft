package org.workcraft.plugins.son.tools;

import org.workcraft.Command;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.plugins.son.SON;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class TimeValueEstimator implements Command {

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, SON.class);
    }

    @Override
    public String getSection() {
        return "Time analysis";
    }

    @Override
    public String getDisplayName() {
        return "Estimate unspecified values";
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final ToolboxPanel toolbox = ToolManager.getToolboxPanel(we);
        final TimeValueSetterTool tool = toolbox.getToolInstance(TimeValueSetterTool.class);
        toolbox.selectTool(tool);
        return we;
    }

}
