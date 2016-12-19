package org.workcraft.plugins.petri.commands;

import java.util.Set;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.commands.AbstractMergeTransformationCommand;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public final class MergeTransitionTransformationCommand extends AbstractMergeTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Merge selected transitions";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualPetriNet.class);
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public Set<Class<? extends VisualComponent>> getMergableClasses() {
        Set<Class<? extends VisualComponent>> result = super.getMergableClasses();
        result.add(VisualTransition.class);
        return result;
    }

}
