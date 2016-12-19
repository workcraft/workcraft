package org.workcraft.plugins.policy.commands;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.tasks.CheckDeadlockTask;
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
    public void run(WorkspaceEntry we) {
        final CheckDeadlockTask task = new CheckDeadlockTask(we);
        String description = "MPSat tool chain";
        String title = we.getTitle();
        if (!title.isEmpty()) {
            description += "(" + title + ")";
        }
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final MpsatChainResultHandler monitor = new MpsatChainResultHandler(task);
        taskManager.queue(task, description, monitor);
    }

}
