package org.workcraft.plugins.petrify.commands;

import java.util.LinkedList;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractSynthesisCommand;
import org.workcraft.plugins.petrify.tasks.PetrifySynthesisResultHandler;
import org.workcraft.plugins.petrify.tasks.PetrifySynthesisTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class PetrifyAbstractSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueSynthesis(we);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        WorkspaceEntry result = null;
        PetrifySynthesisResultHandler monitor = queueSynthesis(we);
        if (monitor != null) {
            result = monitor.waitForHandledResult();
        }
        return result;
    }

    private PetrifySynthesisResultHandler queueSynthesis(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Mutex> mutexes = MutexUtils.getImplementableMutexes(stg);
        if (mutexes == null) {
            return null;
        }
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();
        PetrifySynthesisTask task = new PetrifySynthesisTask(we, getSynthesisParameter(), mutexes);
        PetrifySynthesisResultHandler monitor = new PetrifySynthesisResultHandler(we,
                boxSequentialComponents(), boxCombinationalComponents(), sequentialAssign(),
                technologyMapping(), mutexes);

        taskManager.queue(task, "Petrify logic synthesis", monitor);
        return monitor;
    }

    public boolean boxSequentialComponents() {
        return false;
    }

    public boolean boxCombinationalComponents() {
        return false;
    }

    public boolean sequentialAssign() {
        return false;
    }

    public boolean technologyMapping() {
        return false;
    }

    public abstract String[] getSynthesisParameter();

}
