package org.workcraft;

import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractConversionCommand extends AbstractPromotedCommand implements MenuOrdering {

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
        final ModelEntry meDst = convert(me);
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        final WorkspaceEntry we = workspace.getWorkspaceEntry(me);
        addToWorkspace(meDst, we);
        return meDst;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final ModelEntry me = convert(we.getModelEntry());
        return addToWorkspace(me, we);
    }

    private WorkspaceEntry addToWorkspace(final ModelEntry meDst, WorkspaceEntry weSrc) {
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        final Path<String> directory = weSrc.getWorkspacePath().getParent();
        final String name = weSrc.getWorkspacePath().getNode();
        final boolean openInEditor = meDst.isVisual() || CommonEditorSettings.getOpenNonvisual();
        return workspace.add(directory, name, meDst, false, openInEditor);
    }

    public abstract ModelEntry convert(ModelEntry me);

}
