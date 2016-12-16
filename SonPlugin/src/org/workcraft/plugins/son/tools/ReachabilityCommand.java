package org.workcraft.plugins.son.tools;

import org.workcraft.Command;
import org.workcraft.Framework;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.tasks.ReachabilityTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ReachabilityCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public String getSection() {
        return "Verification";
    }

    @Override
    public String getDisplayName() {
        return "Reachability";
    }

    @Override
    public void run(WorkspaceEntry we) {
        ReachabilityTask task = new ReachabilityTask(we);
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        taskManager.queue(task, "Verification");
    }

}
