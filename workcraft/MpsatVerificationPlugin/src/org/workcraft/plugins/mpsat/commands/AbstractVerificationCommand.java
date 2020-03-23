package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.VerificationChainTask;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public abstract class AbstractVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueVerification(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        VerificationChainResultHandler monitor = queueVerification(we);
        Result<? extends VerificationChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getChainOutcome(result);
    }

    private VerificationChainResultHandler queueVerification(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return null;
        }
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        VerificationParameters verificationParameters = getVerificationParameters(we);
        VerificationChainTask task = new VerificationChainTask(we, verificationParameters);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        VerificationChainResultHandler monitor = new VerificationChainResultHandler(we);
        manager.queue(task, description, monitor);
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
