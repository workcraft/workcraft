package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandlingMonitor;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;

public class NetConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Net synthesis [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class) || WorkspaceUtils.isApplicable(we, Fsm.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        Collection<Mutex> mutexes = getMutexes(we);
        ArrayList<String> args = getArgs();
        TransformationTask task = new TransformationTask(we, args, mutexes);

        boolean hasSignals = hasSignals(we);
        TransformationResultHandlingMonitor monitor = new TransformationResultHandlingMonitor(we, !hasSignals, mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        taskManager.execute(task, "Petrify net synthesis", monitor);
        return monitor.waitForHandledResult();
    }

}
