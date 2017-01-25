package org.workcraft.plugins.dfs.commands;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractVerificationCommand;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.tasks.CheckDataflowTask;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class DfsCombinedVerificationCommand extends AbstractVerificationCommand {

    public String getDisplayName() {
        return "Deadlock and output persistency (reuse unfolding) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Dfs.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final CheckDataflowTask task = new CheckDataflowTask(we);
        String description = "MPSat tool chain";
        String title = we.getTitle();
        if (!title.isEmpty()) {
            description += " (" + title + ")";
        }
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final MpsatChainResultHandler monitor = new MpsatChainResultHandler(task);
        taskManager.queue(task, description, monitor);
    }

}
