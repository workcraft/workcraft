package org.workcraft.plugins.son.commands;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.tasks.ReachabilityTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ReachabilityCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public Section getSection() {
        return new Section("Verification");
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
