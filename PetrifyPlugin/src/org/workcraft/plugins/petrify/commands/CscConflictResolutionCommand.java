package org.workcraft.plugins.petrify.commands;

import java.util.Collection;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class CscConflictResolutionCommand implements ScriptableCommand<WorkspaceEntry> {

    @Override
    public String getSection() {
        return "Encoding conflicts";
    }

    @Override
    public String getDisplayName() {
        return "Resolve CSC conflicts [Petrify]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueCscConflictResolution(we);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        TransformationResultHandler monitor = queueCscConflictResolution(we);
        return monitor.waitForHandledResult();
    }

    private TransformationResultHandler queueCscConflictResolution(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        TransformationTask task = new TransformationTask(
                we, "CSC conflicts resolution", new String[] {"-csc"}, mutexes);

        TransformationResultHandler monitor = new TransformationResultHandler(we, false, mutexes);
        taskManager.queue(task, "Petrify CSC conflicts resolution", monitor);
        return monitor;
    }

}
