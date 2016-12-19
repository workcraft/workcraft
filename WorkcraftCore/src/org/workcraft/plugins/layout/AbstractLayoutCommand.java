package org.workcraft.plugins.layout;

import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class AbstractLayoutCommand implements Command {

    @Override
    public String getSection() {
        return "Graph layout";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            LogUtils.logErrorLine("Tool '" + getClass().getSimpleName() + "' only works in GUI mode.");
        } else {
            final MainWindow mainWindow = framework.getMainWindow();
            final GraphEditorPanel editor = mainWindow.getCurrentEditor();
            we.saveMemento();
            VisualModel model = WorkspaceUtils.getAs(we, VisualModel.class);
            layout(model);
            editor.zoomFit();
        }
    }

    public abstract void layout(VisualModel model);

}
