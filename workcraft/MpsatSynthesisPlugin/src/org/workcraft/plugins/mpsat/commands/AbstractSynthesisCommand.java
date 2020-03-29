package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.SynthesisMode;
import org.workcraft.plugins.mpsat.SynthesisParameters;
import org.workcraft.plugins.mpsat.tasks.SynthesisChainTask;
import org.workcraft.plugins.mpsat.tasks.SynthesisResultHandlingMonitor;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.LinkedList;

/*
  To get Verilog from mpsat, just specify the output file with the extension *.v:
    mpsat -E -! file.bp.pnml file.cg.v
    mpsat -G -! file.bp.pnml file.gC.v
    mpsat -S -! file.bp.pnml file.stdC.v
    mpsat -T -f -p2 -cl -! file.bp.pnml file.mapped.v

  To feed a gate library, use the -d option:
    mpsat -T -f -p2 -cl -! -d gate_library.lib file.bp.pnml file.mapped.v
*/

public abstract class AbstractSynthesisCommand extends  org.workcraft.commands.AbstractSynthesisCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
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
        if (!checkPrerequisites(we)) {
            return null;
        }
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Mutex> mutexes = MutexUtils.getImplementableMutexes(stg);
        if (mutexes == null) {
            return null;
        }
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        TaskManager manager = Framework.getInstance().getTaskManager();
        SynthesisParameters synthesisParameters = getSynthesisParameters();
        SynthesisChainTask task = new SynthesisChainTask(we, synthesisParameters, mutexes);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        SynthesisResultHandlingMonitor monitor = new SynthesisResultHandlingMonitor(we, mutexes);
        manager.queue(task, description, monitor);
        return monitor;
    }

    public boolean checkPrerequisites(WorkspaceEntry we) {
        StgModel net = WorkspaceUtils.getAs(we, StgModel.class);
        return PetriUtils.checkSoundness(net, true);
    }

    private SynthesisParameters getSynthesisParameters() {
        SynthesisMode mode = getSynthesisMode();
        return new SynthesisParameters(mode.toString(), mode, 0);
    }

    public abstract SynthesisMode getSynthesisMode();

}
