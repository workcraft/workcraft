package org.workcraft.plugins.petri.commands;

import java.util.Set;

import org.workcraft.commands.AbstractMergeTransformationCommand;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public final class MergeTransitionTransformationCommand extends AbstractMergeTransformationCommand {

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

    @Override
    public Set<Class<? extends VisualComponent>> getMergableClasses() {
        Set<Class<? extends VisualComponent>> result = super.getMergableClasses();
        result.add(VisualTransition.class);
        return result;
    }

}
