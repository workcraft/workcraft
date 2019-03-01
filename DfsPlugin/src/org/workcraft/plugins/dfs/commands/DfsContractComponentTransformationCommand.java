package org.workcraft.plugins.dfs.commands;

import org.workcraft.commands.AbstractContractTransformationCommand;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class DfsContractComponentTransformationCommand extends AbstractContractTransformationCommand {
    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Dfs.class);
    }
}
