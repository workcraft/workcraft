package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandlingMonitor;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Arrays;
import java.util.Collection;

public class CscConflictResolutionCommand implements ScriptableCommand<WorkspaceEntry> {

    private static final String TITLE = "Resolution of CSC conflicts";

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
        return queueCscConflictResolution(we).waitForHandledResult();
    }

    private TransformationResultHandlingMonitor queueCscConflictResolution(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        TaskManager taskManager = Framework.getInstance().getTaskManager();
        TransformationTask task = new TransformationTask(we, Arrays.asList("-csc"), mutexes);
        TransformationResultHandlingMonitor monitor = new TransformationResultHandlingMonitor(we, false, mutexes);
        taskManager.queue(task, TITLE, monitor);
        return monitor;
    }

}
