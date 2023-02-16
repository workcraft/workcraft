package org.workcraft.commands;

import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractLayoutCommand implements ScriptableCommand<Void> {

    private static final String SECTION_TITLE = "Graph layout";

    @Override
    public final String getSection() {
        return SECTION_TITLE;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualModel.class);
    }

    @Override
    public final void run(WorkspaceEntry we) {
        // Layouts should run synchronously (blocking the editor) as they alter the
        // model, therefore execute method (of ScriptableCommand interface) is called.
        execute(we);
    }

    @Override
    public final Void execute(WorkspaceEntry we) {
        we.saveMemento();
        VisualModel model = WorkspaceUtils.getAs(we, VisualModel.class);
        layout(model);
        final Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            framework.getMainWindow().getCurrentEditor().zoomFit();
        }
        return null;
    }

    public abstract void layout(VisualModel model);

}
