package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainTask;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand implements ScriptableCommand<Boolean> {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueTask(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = queueTask(we);
        monitor.setInteractive(false);
        return monitor.waitForHandledResult();
    }

    private VerificationChainResultHandlingMonitor queueTask(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we);
        if (!checkPrerequisites(we)) {
            monitor.isFinished(Result.cancel());
        } else {
            TaskManager manager = Framework.getInstance().getTaskManager();
            VerificationParameters verificationParameters = getVerificationParameters(we);
            VerificationChainTask task = new VerificationChainTask(we, verificationParameters);
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            manager.queue(task, description, monitor);
        }
        return monitor;
    }

    public boolean checkPrerequisites(WorkspaceEntry we) {
        if (isApplicableTo(we)) {
            PetriModel net = WorkspaceUtils.getAs(we, PetriModel.class);
            if (net != null) {
                return PetriUtils.checkSoundness(net, true);
            }
        }
        return false;
    }

    public abstract VerificationParameters getVerificationParameters(WorkspaceEntry we);

}
