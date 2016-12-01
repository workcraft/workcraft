package org.workcraft.plugins.layout;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractLayoutTool implements Tool {

    @Override
    public String getSection() {
        return "Graph layout";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, VisualModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        we.saveMemento();
        apply(we.getModelEntry());
    }

    @Override
    public ModelEntry apply(ModelEntry me) {
        VisualModel model = WorkspaceUtils.getAs(me, VisualModel.class);
        layout(model);
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (mainWindow != null) {
            GraphEditor editor = mainWindow.getCurrentEditor();
            if (editor != null) {
                editor.zoomFit();
            }
        }
        return me;
    }

    public abstract void layout(VisualModel model);

}
