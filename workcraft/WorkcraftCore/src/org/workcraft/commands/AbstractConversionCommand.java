package org.workcraft.commands;

import org.workcraft.Framework;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractConversionCommand implements ScriptableCommand<WorkspaceEntry> {

    public static final Category CATEGORY = new Category("Conversion", 8);

    @Override
    public final Category getCategory() {
        return CATEGORY;
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
