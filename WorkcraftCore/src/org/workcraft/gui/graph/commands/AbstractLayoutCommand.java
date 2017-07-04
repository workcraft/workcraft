package org.workcraft.gui.graph.commands;

import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class AbstractLayoutCommand implements ScriptableCommand {

    @Override
    public final String getSection() {
        return "Graph layout";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualModel.class);
    }

    @Override
    public final WorkspaceEntry execute(WorkspaceEntry we) {
        we.saveMemento();
        VisualModel model = WorkspaceUtils.getAs(we, VisualModel.class);
        layout(model);
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        if (framework.isInGuiMode() && (mainWindow != null)) {
            final GraphEditorPanel editor = mainWindow.getCurrentEditor();
            editor.zoomFit();
        }
        return we;
    }

    @Override
    public final void run(WorkspaceEntry we) {
        execute(we);
    }

    public abstract void layout(VisualModel model);

}
