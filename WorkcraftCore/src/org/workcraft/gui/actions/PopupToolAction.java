package org.workcraft.gui.actions;

import org.workcraft.Framework;
import org.workcraft.NodeTransformer;
import org.workcraft.gui.graph.commands.Command;

public class PopupToolAction extends Action {
    Command tool;

    public PopupToolAction(Command tool) {
        this.tool = tool;
    }

    public String getText() {
        String text = (tool instanceof NodeTransformer) ? ((NodeTransformer) tool).getPopupName() : tool.getDisplayName();
        return text.trim();
    }

    @Override
    public void run() {
        final Framework framework = Framework.getInstance();
        framework.getMainWindow().runCommand(tool);
    }

}
