package org.workcraft.plugins.dfs.tools;

import org.workcraft.AbstractVerificationCommand;
import org.workcraft.Framework;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.tasks.CheckDataflowDeadlockTask;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class DfsDeadlockVerificationCommand extends AbstractVerificationCommand {

    public String getDisplayName() {
        return "Deadlock [MPSat]";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, Dfs.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final CheckDataflowDeadlockTask task = new CheckDataflowDeadlockTask(we);
        String description = "MPSat tool chain";
        String title = we.getTitle();
        if (!title.isEmpty()) {
            description += "(" + title + ")";
        }
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final MpsatChainResultHandler monitor = new MpsatChainResultHandler(task);
        taskManager.queue(task, description, monitor);
        return we;
    }

}
