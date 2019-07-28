package org.workcraft.commands;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.utils.CommandUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractConversionCommand implements ScriptableCommand<WorkspaceEntry>, MenuOrdering {

    public static final String SECTION_TITLE = CommandUtils.makePromotedSectionTitle("Conversion", 1);

    @Override
    public final String getSection() {
        return SECTION_TITLE;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        final ModelEntry meDst = convert(we.getModelEntry());
        if (meDst == null) {
            return null;
        } else {
            final Framework framework = Framework.getInstance();
            final Path<String> desiredPath = we.getWorkspacePath();
            return framework.createWork(meDst, desiredPath);
        }
    }

    public abstract ModelEntry convert(ModelEntry me);

}
