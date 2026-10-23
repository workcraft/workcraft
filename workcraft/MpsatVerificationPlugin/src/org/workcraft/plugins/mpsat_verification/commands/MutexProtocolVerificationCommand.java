package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.commands.TimeConsumingCommand;
import org.workcraft.plugins.mpsat_verification.StgVerificationGoals;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.CombinedChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.CombinedChainTask;
import org.workcraft.plugins.mpsat_verification.utils.CachedVerificationUtils;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.List;

public class MutexProtocolVerificationCommand
        extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableCommand<Boolean>, TimeConsumingCommand {

    @Override
    public Section getSection() {
        return AbstractEssentialVerificationCommand.SECTION;
    }

    @Override
    public String getDisplayName() {
        return "Mutex protocol";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 60;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueTask(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        CombinedChainResultHandlingMonitor monitor = queueTask(we, false, true);
        return monitor.waitForHandledResult();
    }

    private CombinedChainResultHandlingMonitor queueTask(WorkspaceEntry we) {
        return queueTask(we, true, true);
    }

    private CombinedChainResultHandlingMonitor queueTask(WorkspaceEntry we, boolean interactive,
            boolean reuseCachedResults) {

        CombinedChainResultHandlingMonitor monitor = new CombinedChainResultHandlingMonitor(we);
        monitor.setInteractive(interactive);
        if (!isApplicableTo(we)) {
            monitor.isFinished(Result.cancel());
            return monitor;
        }

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        // There is nothing to verify, and therefore no need to question the soundness of the model, if the property
        // holds vacuously
        if (!StgVerificationGoals.MUTEX_PROTOCOL.isVacuousFor(we) && !PetriUtils.checkSoundness(stg, true)) {
            monitor.isFinished(Result.cancel());
            return monitor;
        }
        // Absence of mutex places is not an issue, as the property then holds vacuously and its result is cached
        if (!MutexUtils.mutexStructuralCheck(stg, true)) {
            monitor.isFinished(Result.cancel());
            return monitor;
        }

        if (CachedVerificationUtils.tryReuse(monitor, CachedVerificationUtils.create(we, this),
                reuseCachedResults, () -> queueTask(we, true, false))) {

            return monitor;
        }

        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        List<VerificationParameters> verificationParametersList = ReachUtils.getMutexProtocolParameters(mutexes);
        CombinedChainTask task = new CombinedChainTask(we, verificationParametersList);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        manager.queue(task, description, monitor);
        return monitor;
    }

}
