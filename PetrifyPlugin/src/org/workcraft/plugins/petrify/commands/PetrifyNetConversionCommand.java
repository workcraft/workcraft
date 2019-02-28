package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.ArrayList;
import java.util.Collection;

public class PetrifyNetConversionCommand extends PetrifyAbstractConversionCommand {

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
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class) || WorkspaceUtils.isApplicable(we, Fsm.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        Collection<Mutex> mutexes = getMutexes(we);
        ArrayList<String> args = getArgs();
        PetrifyTransformationTask task = new PetrifyTransformationTask(
                we, "Net synthesis", args.toArray(new String[args.size()]), mutexes);

        boolean hasSignals = hasSignals(we);
        PetrifyTransformationResultHandler monitor = new PetrifyTransformationResultHandler(we, !hasSignals, mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        taskManager.execute(task, "Petrify net synthesis", monitor);
        return monitor.waitForHandledResult();
    }

}
