package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class MpsatAbstractVerificationCommand extends AbstractVerificationCommand {

    @Override
    public void run(WorkspaceEntry we) {
        final MpsatParameters settings = getSettings(we);
        final MpsatChainTask mpsatTask = new MpsatChainTask(we, settings);

        String description = "MPSat tool chain";
        String title = we.getTitle();
        if (!title.isEmpty()) {
            description += "(" + title + ")";
        }
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final MpsatChainResultHandler monitor = new MpsatChainResultHandler(mpsatTask);
        taskManager.queue(mpsatTask, description, monitor);
    }

    public abstract MpsatParameters getSettings(WorkspaceEntry we);

}
