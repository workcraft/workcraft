package org.workcraft.plugins.mpsat.tools;

import org.workcraft.AbstractVerificationCommand;
import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractMpsatVerificationCommand extends AbstractVerificationCommand {

    @Override
    public abstract String getDisplayName();

    @Override
    public abstract boolean isApplicableTo(ModelEntry me);

    public abstract MpsatSettings getSettings();

    @Override
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final MpsatSettings settings = getSettings();
        final MpsatChainTask mpsatTask = new MpsatChainTask(we, settings);

        String description = "MPSat tool chain";
        String title = we.getTitle();
        if (!title.isEmpty()) {
            description += "(" + title + ")";
        }
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        MpsatChainResultHandler monitor = new MpsatChainResultHandler(mpsatTask);
        taskManager.queue(mpsatTask, description, monitor);
        return we;
    }

}
