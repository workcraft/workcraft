package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.ArrayList;
import java.util.Collection;

public class MpsatMutexImplementabilityVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Mutex place implementability [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueVerification(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        MpsatCombinedChainResultHandler monitor = queueVerification(we);
        Result<? extends MpsatCombinedChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getCombinedChainOutcome(result);
    }

    private MpsatCombinedChainResultHandler queueVerification(WorkspaceEntry we) {
        MpsatCombinedChainResultHandler monitor = null;
        if (isApplicableTo(we)) {
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            if (MpsatUtils.mutexStructuralCheck(stg, false)) {
                Framework framework = Framework.getInstance();
                TaskManager manager = framework.getTaskManager();
                Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
                MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
                ArrayList<MpsatParameters> settingsList = MpsatUtils.getMutexImplementabilitySettings(mutexes);
                MpsatCombinedChainTask task = new MpsatCombinedChainTask(we, settingsList);
                String description = MpsatUtils.getToolchainDescription(we.getTitle());
                monitor = new MpsatCombinedChainResultHandler(task, mutexes);
                manager.queue(task, description, monitor);
            }
        }
        return monitor;
    }

}