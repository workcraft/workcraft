package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandlingMonitor;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Arrays;
import java.util.Collection;

public class UntoggleConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Untoggle signal transitions [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        Collection<Mutex> mutexes = getMutexes(we);
        TransformationTask task = new TransformationTask(we, Arrays.asList("-untog"), mutexes);

        boolean hasSignals = hasSignals(we);
        TransformationResultHandlingMonitor monitor = new TransformationResultHandlingMonitor(we, !hasSignals, mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        taskManager.execute(task, "Petrify signal transition untoggle", monitor);
        return monitor.waitForHandledResult();
    }

}
