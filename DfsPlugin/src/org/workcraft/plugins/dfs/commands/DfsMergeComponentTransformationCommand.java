package org.workcraft.plugins.dfs.commands;

import java.util.Set;

import org.workcraft.commands.AbstractMergeTransformationCommand;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.dfs.VisualControlRegister;
import org.workcraft.plugins.dfs.VisualCounterflowLogic;
import org.workcraft.plugins.dfs.VisualCounterflowRegister;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.VisualLogic;
import org.workcraft.plugins.dfs.VisualPopRegister;
import org.workcraft.plugins.dfs.VisualPushRegister;
import org.workcraft.plugins.dfs.VisualRegister;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public final class DfsMergeComponentTransformationCommand extends AbstractMergeTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Merge selected components";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualDfs.class);
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
