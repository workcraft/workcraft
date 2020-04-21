package org.workcraft.plugins.policy.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.policy.Policy;
import org.workcraft.plugins.policy.tasks.DeadlockFreenessTask;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class DeadlockFreenessVerificationCommand extends AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

    @Override
    public String getDisplayName() {
        return "Deadlock with bundles [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Policy.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
        queueVerification(we, monitor);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, false);
        queueVerification(we, monitor);
        return monitor.waitForHandledResult();
    }

    private void queueVerification(WorkspaceEntry we, VerificationChainResultHandlingMonitor monitor) {
        if (!isApplicableTo(we)) {
            monitor.isFinished(Result.failure());
        } else {
            Framework framework = Framework.getInstance();
            TaskManager manager = framework.getTaskManager();
            DeadlockFreenessTask task = new DeadlockFreenessTask(we);
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            manager.queue(task, description, monitor);
        }
    }

}
