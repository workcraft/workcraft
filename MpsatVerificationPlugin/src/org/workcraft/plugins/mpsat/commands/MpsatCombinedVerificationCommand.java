package org.workcraft.plugins.mpsat.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatCombinedChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatUtils;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainTask;
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
        return "Consistency, deadlock freeness, input properness and output persistency (reuse unfolding) [MPSat]";
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
        return null;
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        final ArrayList<MpsatParameters> settingsList = new ArrayList<>();
        settingsList.add(MpsatParameters.getConsistencySettings());
        settingsList.add(MpsatParameters.getDeadlockSettings());
        settingsList.add(MpsatParameters.getInputPropernessSettings());

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Pair<String, String>> exceptions = MutexUtils.getMutexGrantPairs(stg);
        settingsList.add(MpsatParameters.getOutputPersistencySettings(exceptions));

        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        MpsatCombinedChainTask task = new MpsatCombinedChainTask(we, settingsList);
        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        Result<? extends MpsatCombinedChainResult> result = manager.execute(task, description);
        return MpsatUtils.getCombinedChainOutcome(result);
    }

    @Override
    public final void run(WorkspaceEntry we) {
        final ArrayList<MpsatParameters> settingsList = new ArrayList<>();
        settingsList.add(MpsatParameters.getConsistencySettings());
        settingsList.add(MpsatParameters.getDeadlockSettings());
        settingsList.add(MpsatParameters.getInputPropernessSettings());

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Pair<String, String>> exceptions = MutexUtils.getMutexGrantPairs(stg);
        settingsList.add(MpsatParameters.getOutputPersistencySettings(exceptions));

        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        MpsatCombinedChainTask task = new MpsatCombinedChainTask(we, settingsList);
        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        MpsatCombinedChainResultHandler monitor = new MpsatCombinedChainResultHandler(task, mutexes);
        manager.queue(task, description, monitor);
    }

}
