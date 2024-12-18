package org.workcraft.plugins.dfs.commands;

import org.workcraft.commands.AbstractSplitTransformationCommand;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.dfs.*;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public final class SplitComponentTransformationCommand extends AbstractSplitTransformationCommand {

    public SplitComponentTransformationCommand() {
        registerSplittableClass(VisualRegister.class);
        registerSplittableClass(VisualLogic.class);
        registerSplittableClass(VisualCounterflowLogic.class);
        registerSplittableClass(VisualCounterflowRegister.class);
        registerSplittableClass(VisualControlRegister.class);
        registerSplittableClass(VisualPushRegister.class);
        registerSplittableClass(VisualPopRegister.class);
    }

    @Override
    public String getDisplayName() {
        return "Split selected components";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Split component";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualDfs.class);
    }

}
