package org.workcraft.plugins.fsm.commands;

import org.workcraft.commands.AbstractMergeTransformationCommand;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Set;

public final class MergeStateTransformationCommand extends AbstractMergeTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Merge selected states";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualFsm.class);
    }

    @Override
    public Set<Class<? extends VisualComponent>> getMergableClasses() {
        Set<Class<? extends VisualComponent>> result = super.getMergableClasses();
        result.add(VisualState.class);
        return result;
    }

    @Override
    public VisualConnection createMergedConnection(VisualModel model, VisualConnection connection,
            VisualComponent component, VisualComponent newComponent) {

        VisualConnection newConnection = super.createMergedConnection(model, connection, component, newComponent);
        if ((connection instanceof VisualEvent) && (newConnection instanceof VisualEvent)) {
            VisualEvent event = (VisualEvent) connection;
            VisualEvent newEvent = (VisualEvent) newConnection;
            newEvent.getReferencedConnection().setSymbol(event.getReferencedConnection().getSymbol());
        }
        return newConnection;
    }

}
