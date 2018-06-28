package org.workcraft.plugins.petri.commands;

import java.util.Set;

import org.workcraft.commands.AbstractMergeTransformationCommand;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public final class MergePlaceTransformationCommand extends AbstractMergeTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Merge selected places";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public Set<Class<? extends VisualComponent>> getMergableClasses() {
        Set<Class<? extends VisualComponent>> result = super.getMergableClasses();
        result.add(VisualPlace.class);
        return result;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

}
