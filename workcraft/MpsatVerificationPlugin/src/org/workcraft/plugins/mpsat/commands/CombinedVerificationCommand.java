package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.CombinedChainOutput;
import org.workcraft.plugins.mpsat.tasks.CombinedChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.CombinedChainTask;
import org.workcraft.plugins.mpsat.tasks.OutputDeterminacyTask;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class CombinedVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand implements ScriptableCommand<Boolean> {

    @Override
    public String getDisplayName() {
        return "All of the above (reuse unfolding) [MPSat]";
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
        CombinedChainResultHandler monitor = queueVerification(we);
        Result<? extends CombinedChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getCombinedChainOutcome(result);
    }

    private CombinedChainResultHandler queueVerification(WorkspaceEntry we) {

        if (!checkPrerequisites(we)) {
            return null;
        }

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);

        boolean noDummies = stg.getDummyTransitions().isEmpty();
        if (!noDummies) {
            String msg = "Input properness and Output persistency\n" +
                    "can currently be checked only for STGs without dummies.\n\n" +
                    "Proceed with verification of other properties?";
            if (!DialogUtils.showConfirmWarning(msg, "Verification", true)) {
                return null;
            }
        }

        if (!MpsatUtils.mutexStructuralCheck(stg, true)) {
            return null;
        }

        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        ArrayList<VerificationParameters> settingsList = new ArrayList<>();
        settingsList.add(ReachUtils.getConsistencySettings());
        settingsList.add(ReachUtils.getDeadlockSettings());
        if (noDummies) {
            settingsList.add(ReachUtils.getInputPropernessSettings());
        }

        settingsList.addAll(ReachUtils.getMutexImplementabilitySettings(mutexes));
        if (noDummies) {
            LinkedList<Pair<String, String>> exceptions = MutexUtils.getMutexGrantPairs(stg);
            settingsList.add(ReachUtils.getOutputPersistencySettings(exceptions));
        }

        OutputDeterminacyTask extraTask = new OutputDeterminacyTask(we);

        TaskManager manager = Framework.getInstance().getTaskManager();
        CombinedChainTask task = new CombinedChainTask(we, settingsList, extraTask);
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        CombinedChainResultHandler monitor = new CombinedChainResultHandler(we, mutexes);
        manager.queue(task, description, monitor);
        return monitor;
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        if (isApplicableTo(we)) {
            Stg net = WorkspaceUtils.getAs(we, Stg.class);
            if (net != null) {
                return PetriUtils.checkSoundness(net, true);
            }
        }
        return false;
    }

}
