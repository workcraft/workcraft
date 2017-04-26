package org.workcraft.plugins.son.commands;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.workspace.WorkspaceEntry;

public class ToolManager {

    public static ToolboxPanel getToolboxPanel(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        GraphEditorPanel editor = mainWindow.getEditor(we);
        return editor.getToolBox();
    }

}
