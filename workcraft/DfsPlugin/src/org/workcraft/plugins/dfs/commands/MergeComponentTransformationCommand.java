package org.workcraft.plugins.dfs.commands;

import org.workcraft.commands.AbstractMergeTransformationCommand;
import org.workcraft.plugins.dfs.*;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public final class MergeComponentTransformationCommand extends AbstractMergeTransformationCommand {

    public MergeComponentTransformationCommand() {
        registerMergableClass(VisualRegister.class);
        registerMergableClass(VisualLogic.class);
        registerMergableClass(VisualCounterflowLogic.class);
        registerMergableClass(VisualCounterflowRegister.class);
        registerMergableClass(VisualControlRegister.class);
        registerMergableClass(VisualPushRegister.class);
        registerMergableClass(VisualPopRegister.class);
    }

    @Override
    public String getDisplayName() {
        return "Merge selected components";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualDfs.class);
    }

}
