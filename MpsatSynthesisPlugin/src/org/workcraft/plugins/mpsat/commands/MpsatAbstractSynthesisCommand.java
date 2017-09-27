package org.workcraft.plugins.mpsat.commands;

import java.util.LinkedList;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractSynthesisCommand;
import org.workcraft.plugins.mpsat.MpsatSynthesisMode;
import org.workcraft.plugins.mpsat.MpsatSynthesisParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatSynthesisChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatSynthesisResultHandler;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
/*
  To get Verilog from mpsat, just specify the output file with the extension *.v:
    mpsat -E -! file.bp.pnml file.cg.v
    mpsat -G -! file.bp.pnml file.gC.v
    mpsat -S -! file.bp.pnml file.stdC.v
    mpsat -T -f -p2 -cl -! file.bp.pnml file.mapped.v

  To feed a gate library, use the -d option:
    mpsat -T -f -p2 -cl -! -d gate_library.lib file.bp.pnml file.mapped.v
*/
import org.workcraft.workspace.WorkspaceUtils;

public abstract class MpsatAbstractSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Mutex> mutexes = MutexUtils.getImplementableMutexes(stg);
        if (mutexes == null) {
            return null;
        }
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final MpsatSynthesisParameters settings = new MpsatSynthesisParameters("Logic synthesis", getSynthesisMode(), 0);
        final MpsatSynthesisChainTask task = new MpsatSynthesisChainTask(we, settings, mutexes);
        final MpsatSynthesisResultHandler monitor = new MpsatSynthesisResultHandler(task, mutexes);
        taskManager.execute(task, "MPSat logic synthesis", monitor);
        return monitor.getResult();
    }

    @Override
    public void run(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Mutex> mutexes = MutexUtils.getImplementableMutexes(stg);
        if (mutexes == null) {
            return;
        }
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final MpsatSynthesisParameters settings = new MpsatSynthesisParameters("Logic synthesis", getSynthesisMode(), 0);
        final MpsatSynthesisChainTask task = new MpsatSynthesisChainTask(we, settings, mutexes);
        final MpsatSynthesisResultHandler monitor = new MpsatSynthesisResultHandler(task, mutexes);
        taskManager.queue(task, "MPSat logic synthesis", monitor);
    }

    public abstract MpsatSynthesisMode getSynthesisMode();

}
