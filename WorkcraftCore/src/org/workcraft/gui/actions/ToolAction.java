package org.workcraft.gui.actions;

import org.workcraft.Framework;
import org.workcraft.Tool;

public class ToolAction extends Action {
    Tool tool;

    public ToolAction(Tool tool) {
        this.tool = tool;
    }

    public String getText() {
        return tool.getDisplayName().trim();
    }

    @Override
    public void run() {
        final Framework framework = Framework.getInstance();
        framework.getMainWindow().runTool(tool);
    }

}
