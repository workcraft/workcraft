package org.workcraft.plugins.son.tools;

import java.util.List;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.workspace.WorkspaceEntry;

public class ToolManager {

    public static ToolboxPanel getToolboxPanel(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        GraphEditorPanel currentEditor = mainWindow.getCurrentEditor();
        if (currentEditor == null || currentEditor.getWorkspaceEntry() != we) {
            final List<GraphEditorPanel> editors = mainWindow.getEditors(we);
            if (editors.size() > 0) {
                currentEditor = editors.get(0);
                mainWindow.requestFocus(currentEditor);
            } else {
                currentEditor = mainWindow.createEditorWindow(we);
            }
        }
        return currentEditor.getToolBox();
    }

}
