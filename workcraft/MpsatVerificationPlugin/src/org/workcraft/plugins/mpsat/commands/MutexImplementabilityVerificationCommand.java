package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.CombinedChainOutput;
import org.workcraft.plugins.mpsat.tasks.CombinedChainResultHandler;
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
        queueVerification(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        CombinedChainResultHandler monitor = queueVerification(we);
        Result<? extends CombinedChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getCombinedChainOutcome(result);
    }

    private CombinedChainResultHandler queueVerification(WorkspaceEntry we) {
        CombinedChainResultHandler monitor = null;
        if (isApplicableTo(we)) {
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            if (MpsatUtils.mutexStructuralCheck(stg, false)) {
                Framework framework = Framework.getInstance();
                TaskManager manager = framework.getTaskManager();
                Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
                MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
                ArrayList<VerificationParameters> settingsList = ReachUtils.getMutexImplementabilitySettings(mutexes);
                CombinedChainTask task = new CombinedChainTask(we, settingsList, null);
                String description = MpsatUtils.getToolchainDescription(we.getTitle());
                monitor = new CombinedChainResultHandler(we, mutexes);
                manager.queue(task, description, monitor);
            }
        }
        return monitor;
    }

}