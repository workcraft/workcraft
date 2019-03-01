package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.*;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.types.Pair;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class MpsatCombinedVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Consistency, deadlock freeness, input properness, mutex implementability, output persistency, output determinacy [MPSat]";
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
        ArrayList<MpsatParameters> settingsList = new ArrayList<>();
        settingsList.add(MpsatParameters.getConsistencySettings());
        settingsList.add(MpsatParameters.getDeadlockSettings());
        if (noDummies) {
            settingsList.add(MpsatParameters.getInputPropernessSettings());
        }

        settingsList.addAll(MpsatUtils.getMutexImplementabilitySettings(mutexes));
        if (noDummies) {
            LinkedList<Pair<String, String>> exceptions = MutexUtils.getMutexGrantPairs(stg);
            settingsList.add(MpsatParameters.getOutputPersistencySettings(exceptions));
        }

        MpsatOutputDeterminacyTask extraTask = new MpsatOutputDeterminacyTask(we);

        TaskManager manager = Framework.getInstance().getTaskManager();
        MpsatCombinedChainTask task = new MpsatCombinedChainTask(we, settingsList, extraTask);
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        MpsatCombinedChainResultHandler monitor = new MpsatCombinedChainResultHandler(task, mutexes);
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
