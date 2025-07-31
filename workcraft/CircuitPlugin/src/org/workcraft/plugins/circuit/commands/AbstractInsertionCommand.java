package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractInsertionCommand implements ScriptableCommand<Void> {

    @Override
    public final Section getSection() {
        return new Section("Insertion");
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.NEVER;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public final void run(WorkspaceEntry we) {
        // Run synchronously (blocking the editor) as model is changed.
        execute(we);
    }

    @Override
    public final Void execute(WorkspaceEntry we) {
        insert(we);
        return null;
    }

    public abstract void insert(WorkspaceEntry we);

}
