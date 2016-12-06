package org.workcraft;

import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.CommonEditorSettings;
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
    public WorkspaceEntry run(WorkspaceEntry we) {
        final ModelEntry me = run(we.getModelEntry());
        if (me == null) {
            return null;
        } else {
            final Workspace workspace = Framework.getInstance().getWorkspace();
            final Path<String> directory = we.getWorkspacePath().getParent();
            final String name = we.getWorkspacePath().getNode();
            final boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
            return workspace.add(directory, name, me, false, openInEditor);
        }
    }

}
