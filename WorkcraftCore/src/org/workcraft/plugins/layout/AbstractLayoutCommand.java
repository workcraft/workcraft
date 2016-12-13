package org.workcraft.plugins.layout;

import org.workcraft.Command;
import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class AbstractLayoutCommand implements Command {

    @Override
    public String getSection() {
        return "Graph layout";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, VisualModel.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            LogUtils.logErrorLine("Tool '" + getClass().getSimpleName() + "' only works in GUI mode.");
        } else {
            final MainWindow mainWindow = framework.getMainWindow();
            final GraphEditorPanel editor = mainWindow.getCurrentEditor();
            final WorkspaceEntry we = editor.getWorkspaceEntry();
            we.saveMemento();

            VisualModel model = WorkspaceUtils.getAs(me, VisualModel.class);
            layout(model);
            editor.zoomFit();
        }
        return me;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        run(we.getModelEntry());
        return we;
    }

    public abstract void layout(VisualModel model);

}
