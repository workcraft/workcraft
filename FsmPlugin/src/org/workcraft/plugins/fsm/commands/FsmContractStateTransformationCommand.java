package org.workcraft.plugins.fsm.commands;

import org.workcraft.commands.AbstractContractTransformationCommand;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class FsmContractStateTransformationCommand extends AbstractContractTransformationCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Fsm.class);
    }

}
