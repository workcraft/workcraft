package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatUtils;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class MpsatAbstractVerificationCommand extends AbstractVerificationCommand {

    @Override
    public Boolean execute(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        MpsatParameters settings = getSettings(we);
        MpsatChainTask task = new MpsatChainTask(we, settings);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        Result<? extends MpsatChainResult> result = manager.execute(task, description);
        return MpsatUtils.getChainOutcome(result);
    }

    @Override
    public void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        MpsatParameters settings = getSettings(we);
        MpsatChainTask task = new MpsatChainTask(we, settings);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        MpsatChainResultHandler monitor = new MpsatChainResultHandler(task);
        manager.queue(task, description, monitor);
    }

    public abstract MpsatParameters getSettings(WorkspaceEntry we);

}
