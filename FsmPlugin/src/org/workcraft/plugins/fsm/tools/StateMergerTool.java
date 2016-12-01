package org.workcraft.plugins.fsm.tools;

import java.util.Set;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.tools.AbstractMergerTool;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.workspace.ModelEntry;

public final class StateMergerTool extends AbstractMergerTool {

    @Override
    public String getDisplayName() {
        return "Merge selected states";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel() instanceof Fsm;
    }

    @Override
    public Set<Class<? extends VisualComponent>> getMergableClasses() {
        Set<Class<? extends VisualComponent>> result = super.getMergableClasses();
        result.add(VisualState.class);
        return result;
    }

}
