package org.workcraft;

import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class ConversionTool extends PromotedTool implements MenuOrdering {

    @Override
    public String getSection() {
        return "!    Conversion"; // 4 spaces - positions 1st
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        final ModelEntry result = convert(me);
        addToWorkspace(me);
        return result;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final ModelEntry me = convert(we.getModelEntry());
        return addToWorkspace(me);
    }

    private WorkspaceEntry addToWorkspace(final ModelEntry me) {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            LogUtils.logErrorLine("Tool '" + getClass().getSimpleName() + "' only works in GUI mode.");
            return null; // !!!
        } else {
            final Workspace workspace = framework.getWorkspace();
            final MainWindow mainWindow = framework.getMainWindow();
            final WorkspaceEntry we = mainWindow.getCurrentWorkspaceEntry();
            final Path<String> directory = we.getWorkspacePath().getParent();
            final String name = we.getWorkspacePath().getNode();
            final boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
            return workspace.add(directory, name, me, false, openInEditor);
        }
    }

    public abstract ModelEntry convert(ModelEntry me);

}
