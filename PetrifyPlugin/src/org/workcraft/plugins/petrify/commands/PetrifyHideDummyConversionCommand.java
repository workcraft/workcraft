package org.workcraft.plugins.petrify.commands;

import java.util.Collection;

import org.workcraft.Framework;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifyHideDummyConversionCommand extends PetrifyAbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Dummy contraction [Petrify]";
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
        PetrifyTransformationTask task = new PetrifyTransformationTask(
                we, "Dummy contraction", new String[] {"-hide", ".dummy" }, mutexes);

        boolean hasSignals = hasSignals(we);
        PetrifyTransformationResultHandler monitor = new PetrifyTransformationResultHandler(we, !hasSignals, mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        taskManager.execute(task, "Petrify dummy contraction", monitor);
        return monitor.waitForHandledResult();
    }

}
