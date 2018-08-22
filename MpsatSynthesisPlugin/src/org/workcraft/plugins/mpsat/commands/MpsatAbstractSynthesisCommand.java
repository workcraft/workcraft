package org.workcraft.plugins.mpsat.commands;

import java.util.LinkedList;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractSynthesisCommand;
import org.workcraft.plugins.mpsat.MpsatSynthesisMode;
import org.workcraft.plugins.mpsat.MpsatSynthesisParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatSynthesisChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatSynthesisResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
import org.workcraft.plugins.stg.*;
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
    public void run(WorkspaceEntry we) {
        queueSynthesis(we);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        WorkspaceEntry result = null;
        MpsatSynthesisResultHandler monitor = queueSynthesis(we);
        if (monitor != null) {
            result = monitor.waitForHandledResult();
        }
        return result;
    }

    private MpsatSynthesisResultHandler queueSynthesis(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return null;
        }
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Mutex> mutexes = MutexUtils.getImplementableMutexes(stg);
        if (mutexes == null) {
            return null;
        }
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        MpsatSynthesisParameters settings = getSettings();
        MpsatSynthesisChainTask task = new MpsatSynthesisChainTask(we, settings, mutexes);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        MpsatSynthesisResultHandler monitor = new MpsatSynthesisResultHandler(task, mutexes);
        manager.queue(task, description, monitor);
        return monitor;
    }

    public boolean checkPrerequisites(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        return StgUtils.checkStg(stg, true);
    }

    private MpsatSynthesisParameters getSettings() {
        MpsatSynthesisMode mode = getSynthesisMode();
        return new MpsatSynthesisParameters(mode.toString(), mode, 0);
    }

    public abstract MpsatSynthesisMode getSynthesisMode();

}
