package org.workcraft.gui.actions;

import org.workcraft.Framework;
import org.workcraft.NodeTransformer;
import org.workcraft.Tool;

public class PopupToolAction extends Action {
    Tool tool;

    public PopupToolAction(Tool tool) {
        this.tool = tool;
    }

    public String getText() {
        String text = (tool instanceof NodeTransformer) ? ((NodeTransformer) tool).getPopupName() : tool.getDisplayName();
        return text.trim();
    }

    @Override
    public void run() {
        final Framework framework = Framework.getInstance();
        framework.getMainWindow().runTool(tool);
    }

}
