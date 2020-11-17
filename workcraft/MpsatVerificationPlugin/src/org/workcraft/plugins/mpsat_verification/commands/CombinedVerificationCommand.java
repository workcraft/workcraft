package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.CombinedChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.CombinedChainTask;
import org.workcraft.plugins.mpsat_verification.tasks.OutputDeterminacyTask;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
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
        if (!checkPrerequisites(we)) {
            monitor.isFinished(Result.cancel());
            return;
        }

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);

        boolean noDummies = stg.getDummyTransitions().isEmpty();
        if (!noDummies) {
            String msg = "Input properness and Output persistency\n" +
                    "can currently be checked only for STGs without dummies.\n\n" +
                    "Proceed with verification of other properties?";
            if (!DialogUtils.showConfirmWarning(msg)) {
                monitor.isFinished(Result.cancel());
                return;
            }
        }

        if (!MpsatUtils.mutexStructuralCheck(stg, true)) {
            monitor.isFinished(Result.cancel());
            return;
        }

        ArrayList<VerificationParameters> verificationParametersList = new ArrayList<>();
        verificationParametersList.add(ReachUtils.getConsistencyParameters());
        verificationParametersList.add(ReachUtils.getDeadlockParameters());
        if (noDummies) {
            verificationParametersList.add(ReachUtils.getInputPropernessParameters());
        }

        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        verificationParametersList.addAll(ReachUtils.getMutexImplementabilityParameters(mutexes));
        if (noDummies) {
            LinkedList<Pair<String, String>> exceptions = MutexUtils.getMutexGrantPairs(stg);
            verificationParametersList.add(ReachUtils.getOutputPersistencyParameters(exceptions));
        }

        OutputDeterminacyTask extraTask = new OutputDeterminacyTask(we);

        TaskManager manager = Framework.getInstance().getTaskManager();
        CombinedChainTask task = new CombinedChainTask(we, verificationParametersList, extraTask);
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        monitor.setVacuousMutexImplementability(mutexes.isEmpty());

        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        manager.queue(task, description, monitor);
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
