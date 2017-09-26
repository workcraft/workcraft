package org.workcraft.plugins.policy.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatUtils;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.tasks.CheckDeadlockTask;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PolicyDeadlockVerificationCommand extends AbstractVerificationCommand {

    public String getDisplayName() {
        return " Deadlock with bundels [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PolicyNet.class);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        CheckDeadlockTask task = new CheckDeadlockTask(we);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        Result<? extends MpsatChainResult> result = manager.execute(task, description);
        return MpsatUtils.getChainOutcome(result);
    }

    @Override
    public void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        CheckDeadlockTask task = new CheckDeadlockTask(we);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        MpsatChainResultHandler monitor = new MpsatChainResultHandler(task);
        manager.queue(task, description, monitor);
    }

}
