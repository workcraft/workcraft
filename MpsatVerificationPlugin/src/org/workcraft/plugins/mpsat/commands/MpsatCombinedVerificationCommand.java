package org.workcraft.plugins.mpsat.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

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
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatCombinedVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Consistency, deadlock freeness, input properness, output persistency and mutex implementability (reuse unfolding) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public int getPriority() {
        return 1;
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
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        if (MpsatUtils.mutexStructuralCheck(stg, true)) {
            Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
            ArrayList<MpsatParameters> settingsList = new ArrayList<>();
            settingsList.add(MpsatParameters.getConsistencySettings());
            settingsList.add(MpsatParameters.getDeadlockSettings());
            settingsList.add(MpsatParameters.getInputPropernessSettings());
            settingsList.addAll(MpsatUtils.getMutexImplementabilitySettings(mutexes));

            LinkedList<Pair<String, String>> exceptions = MutexUtils.getMutexGrantPairs(stg);
            settingsList.add(MpsatParameters.getOutputPersistencySettings(exceptions));

            Framework framework = Framework.getInstance();
            TaskManager manager = framework.getTaskManager();
            MpsatCombinedChainTask task = new MpsatCombinedChainTask(we, settingsList);
            MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            monitor = new MpsatCombinedChainResultHandler(task, mutexes);
            manager.queue(task, description, monitor);
        }
        return monitor;
    }

}
