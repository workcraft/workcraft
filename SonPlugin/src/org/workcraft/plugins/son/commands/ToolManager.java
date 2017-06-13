package org.workcraft.plugins.son.commands;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.workspace.WorkspaceEntry;

public class ToolManager {

    public static Toolbox getToolboxPanel(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        GraphEditorPanel editor = mainWindow.getEditor(we);
        return editor.getToolBox();
    }

}
