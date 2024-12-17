package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.AbstractMergeTransformationCommand;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public final class MergeTransitionTransformationCommand extends AbstractMergeTransformationCommand {

    public MergeTransitionTransformationCommand() {
        registerMergableClass(VisualTransition.class);
    }

    @Override
    public String getDisplayName() {
        return "Merge selected transitions";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualPetri.class);
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

}
