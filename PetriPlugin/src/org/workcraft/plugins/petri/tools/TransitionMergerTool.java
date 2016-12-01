package org.workcraft.plugins.petri.tools;

import java.util.Set;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.tools.AbstractMergerTool;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.workspace.ModelEntry;

public final class TransitionMergerTool extends AbstractMergerTool {

    @Override
    public String getDisplayName() {
        return "Merge selected transitions";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel() instanceof PetriNet;
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
