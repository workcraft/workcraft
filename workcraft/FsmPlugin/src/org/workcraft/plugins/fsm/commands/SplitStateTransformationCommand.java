package org.workcraft.plugins.fsm.commands;

import org.workcraft.commands.AbstractSplitTransformationCommand;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public final class SplitStateTransformationCommand extends AbstractSplitTransformationCommand {

    public SplitStateTransformationCommand() {
        registerSplittableClass(VisualState.class);
    }

    @Override
    public String getDisplayName() {
        return "Split selected states";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Split state";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualFsm.class);
    }

    @Override
    public VisualComponent createDuplicate(VisualModel model, VisualComponent component) {
        VisualComponent result = super.createDuplicate(model, component);
        if ((component instanceof VisualState state) && (result instanceof VisualState newState)) {
            newState.getReferencedComponent().setInitial(state.getReferencedComponent().isInitial());
        }
        return result;
    }

}
