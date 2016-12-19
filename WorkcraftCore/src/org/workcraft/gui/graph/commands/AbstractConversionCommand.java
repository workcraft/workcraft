package org.workcraft.gui.graph.commands;

import org.workcraft.Framework;
import org.workcraft.MenuOrdering;
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
    public void run(WorkspaceEntry we) {
        final ModelEntry meDst = convert(we.getModelEntry());
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        final Path<String> directory = we.getWorkspacePath().getParent();
        final String name = we.getWorkspacePath().getNode();
        final boolean openInEditor = meDst.isVisual() || CommonEditorSettings.getOpenNonvisual();
        workspace.addWork(directory, name, meDst, false, openInEditor);
    }

    public abstract ModelEntry convert(ModelEntry me);

}
