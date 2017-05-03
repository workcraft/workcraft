package org.workcraft.plugins.petrify.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.Framework;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
        ArrayList<String> args = getArgs();
        final PetrifyTransformationTask task = new PetrifyTransformationTask(we, "Net synthesis", args.toArray(new String[args.size()]));
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        boolean hasSignals = hasSignals(we);
        Collection<Mutex> mutexes = getMutexes(we);
        final PetrifyTransformationResultHandler monitor = new PetrifyTransformationResultHandler(we, !hasSignals, mutexes);
        taskManager.execute(task, "Petrify net synthesis", monitor);
        return monitor.getResult();
    }

}
