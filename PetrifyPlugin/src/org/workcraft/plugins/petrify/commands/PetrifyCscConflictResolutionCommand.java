package org.workcraft.plugins.petrify.commands;

import java.util.Collection;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifyCscConflictResolutionCommand implements ScriptableCommand<WorkspaceEntry> {

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
        PetrifyTransformationResultHandler monitor = queueCscConflictResolution(we);
        return monitor.waitForHandledResult();
    }

    private PetrifyTransformationResultHandler queueCscConflictResolution(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        PetrifyTransformationTask task = new PetrifyTransformationTask(
                we, "CSC conflicts resolution", new String[] {"-csc"}, mutexes);

        PetrifyTransformationResultHandler monitor = new PetrifyTransformationResultHandler(we, false, mutexes);
        taskManager.queue(task, "Petrify CSC conflicts resolution", monitor);
        return monitor;
    }

}
