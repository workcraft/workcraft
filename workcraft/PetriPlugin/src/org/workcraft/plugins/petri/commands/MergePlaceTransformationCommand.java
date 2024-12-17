package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.AbstractMergeTransformationCommand;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public final class MergePlaceTransformationCommand extends AbstractMergeTransformationCommand {

    public MergePlaceTransformationCommand() {
        registerMergableClass(VisualPlace.class);
    }

    @Override
    public String getDisplayName() {
        return "Merge selected places";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

}
