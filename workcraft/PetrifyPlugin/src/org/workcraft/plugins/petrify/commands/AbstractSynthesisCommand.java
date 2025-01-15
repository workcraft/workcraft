package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.utils.ArbitrationUtils;
import org.workcraft.plugins.petrify.tasks.SynthesisResultHandlingMonitor;
import org.workcraft.plugins.petrify.tasks.SynthesisTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.List;

public abstract class AbstractSynthesisCommand extends  org.workcraft.commands.AbstractSynthesisCommand {

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
        SynthesisResultHandlingMonitor monitor = queueSynthesis(we);
        if (monitor != null) {
            result = monitor.waitForHandledResult();
        }
        return result;
    }

    private SynthesisResultHandlingMonitor queueSynthesis(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        List<Mutex> mutexes = ArbitrationUtils.getImplementableMutexesOrNullForError(stg);
        if (mutexes == null) {
            return null;
        }
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();
        SynthesisTask task = new SynthesisTask(we, getSynthesisParameter(), mutexes, technologyMapping());
        SynthesisResultHandlingMonitor monitor = new SynthesisResultHandlingMonitor(we, mutexes,
                boxSequentialComponents(), boxCombinationalComponents(),
                celementAssign(), sequentialAssign(), technologyMapping());

        taskManager.queue(task, "Petrify logic synthesis", monitor);
        return monitor;
    }

    public boolean boxSequentialComponents() {
        return false;
    }

    public boolean boxCombinationalComponents() {
        return false;
    }

    public boolean celementAssign() {
        return false;
    }

    public boolean sequentialAssign() {
        return false;
    }

    public boolean technologyMapping() {
        return false;
    }

    public abstract List<String> getSynthesisParameter();

}
