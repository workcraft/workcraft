package org.workcraft.plugins.fsm.commands;

import org.workcraft.commands.AbstractMergeTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public final class MergeStateTransformationCommand extends AbstractMergeTransformationCommand {

    public MergeStateTransformationCommand() {
        registerMergableClass(VisualState.class);
    }

    @Override
    public String getDisplayName() {
        return "Merge selected states";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualFsm.class);
    }

    @Override
    public VisualConnection createMergedConnection(VisualModel model, VisualConnection connection,
            VisualNode component, VisualNode newComponent) {

        VisualConnection newConnection = super.createMergedConnection(model, connection, component, newComponent);
        if ((connection instanceof VisualEvent event) && (newConnection instanceof VisualEvent newEvent)) {
            newEvent.getReferencedConnection().setSymbol(event.getReferencedConnection().getSymbol());
        }
        return newConnection;
    }

}
