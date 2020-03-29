package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.CombinedChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat.tasks.CombinedChainTask;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;

public class MutexImplementabilityVerificationCommand extends AbstractVerificationCommand implements ScriptableCommand<Boolean> {

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
        return 4;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public void run(WorkspaceEntry we) {
        CombinedChainResultHandlingMonitor monitor = new CombinedChainResultHandlingMonitor(we, true);
        queueVerification(we, monitor);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        CombinedChainResultHandlingMonitor monitor = new CombinedChainResultHandlingMonitor(we, false);
        queueVerification(we, monitor);
        return monitor.waitForHandledResult();
    }

    private void queueVerification(WorkspaceEntry we, CombinedChainResultHandlingMonitor monitor) {
        if (!isApplicableTo(we)) {
            monitor.isFinished(Result.failure());
            return;
        }
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        if (!MpsatUtils.mutexStructuralCheck(stg, false)) {
            monitor.isFinished(Result.failure());
            return;
        }
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        ArrayList<VerificationParameters> settingsList = ReachUtils.getMutexImplementabilityParameters(mutexes);
        CombinedChainTask task = new CombinedChainTask(we, settingsList, null);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        monitor.setMutexes(mutexes);
        manager.queue(task, description, monitor);
    }

}