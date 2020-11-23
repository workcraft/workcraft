package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandlingMonitor;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractConversionCommand extends  org.workcraft.commands.AbstractConversionCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; //Conversion to be performed by Petrify backend
    }

    public ArrayList<String> getArgs(WorkspaceEntry we) {
        ArrayList<String> args = new ArrayList<>();
        args.add("-dead");
        return args;
    }

    public boolean hasSignals(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class) || WorkspaceUtils.isApplicable(we, Fst.class);
    }

    public Collection<Mutex> getMutexes(WorkspaceEntry we) {
        Collection<Mutex> mutexes = null;
        if (WorkspaceUtils.isApplicable(we, Stg.class)) {
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            mutexes = MutexUtils.getMutexes(stg);
        }
        return mutexes;
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        ArrayList<String> args = getArgs(we);
        Collection<Mutex> mutexes = getMutexes(we);
        TransformationTask task = new TransformationTask(we, args, mutexes);

        boolean hasSignals = hasSignals(we);
        TransformationResultHandlingMonitor monitor = new TransformationResultHandlingMonitor(we, !hasSignals, mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        taskManager.execute(task, "Petrify net synthesis", monitor);
        return monitor.waitForHandledResult();
    }

}
