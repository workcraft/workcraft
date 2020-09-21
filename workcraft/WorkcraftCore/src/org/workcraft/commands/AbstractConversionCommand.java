package org.workcraft.commands;

import org.workcraft.Framework;
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
    public final void run(WorkspaceEntry we) {
        // Run synchronously (blocking the editor). Execution must be fast.
        execute(we);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        final ModelEntry me = convert(we.getModelEntry());
        return me == null ? null : Framework.getInstance().createWork(me, we.getFileName());
    }

    public abstract ModelEntry convert(ModelEntry me);

}
