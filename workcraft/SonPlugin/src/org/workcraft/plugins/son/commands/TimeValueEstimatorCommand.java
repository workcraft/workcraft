package org.workcraft.plugins.son.commands;

import org.workcraft.commands.Command;
import org.workcraft.gui.Toolbox;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.tools.TimeValueSetterTool;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeValueEstimatorCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public Section getSection() {
        return new Section("Time analysis");
    }

    @Override
    public String getDisplayName() {
        return "Estimate unspecified values";
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Toolbox toolbox = ToolManager.getToolboxPanel(we);
        final TimeValueSetterTool tool = toolbox.getToolInstance(TimeValueSetterTool.class);
        toolbox.selectTool(tool);
    }

}
