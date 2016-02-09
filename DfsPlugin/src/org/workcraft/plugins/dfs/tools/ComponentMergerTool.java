package org.workcraft.plugins.dfs.tools;

import java.util.Set;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.tools.AbstractMergerTool;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.VisualControlRegister;
import org.workcraft.plugins.dfs.VisualCounterflowLogic;
import org.workcraft.plugins.dfs.VisualCounterflowRegister;
import org.workcraft.plugins.dfs.VisualLogic;
import org.workcraft.plugins.dfs.VisualPopRegister;
import org.workcraft.plugins.dfs.VisualPushRegister;
import org.workcraft.plugins.dfs.VisualRegister;
import org.workcraft.workspace.WorkspaceEntry;

public final class ComponentMergerTool extends AbstractMergerTool {
    @Override
    public String getDisplayName() {
        return "Merge selected components";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Dfs;
    }

    @Override
    public Set<Class<? extends VisualComponent>> getMergableClasses() {
        Set<Class<? extends VisualComponent>> result = super.getMergableClasses();
        result.add(VisualLogic.class);
        result.add(VisualRegister.class);
        result.add(VisualCounterflowLogic.class);
        result.add(VisualCounterflowRegister.class);
        result.add(VisualControlRegister.class);
        result.add(VisualPushRegister.class);
        result.add(VisualPopRegister.class);
        return result;
    }

}
