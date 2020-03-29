package org.workcraft.tasks;

import org.workcraft.Framework;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractOutputInterpreter<T extends ExternalProcessOutput, U> implements OutputInterpreter<T, U> {

    private final WorkspaceEntry we;
    private final T output;
    private final boolean interactive;

    public AbstractOutputInterpreter(WorkspaceEntry we, T output, boolean interactive) {
        this.we = we;
        this.output = output;
        this.interactive = interactive;
    }

    @Override
    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    @Override
    public T getOutput() {
        return output;
    }

    @Override
    public boolean isInteractive() {
        return interactive && Framework.getInstance().isInGuiMode();
    }

}
