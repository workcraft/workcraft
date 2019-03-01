package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatChainOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public abstract class MpsatAbstractVerificationCommand extends AbstractVerificationCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueVerification(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        MpsatChainResultHandler monitor = queueVerification(we);
        Result<? extends MpsatChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getChainOutcome(result);
    }

    private MpsatChainResultHandler queueVerification(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return null;
        }
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        MpsatParameters settings = getSettings(we);
        MpsatChainTask task = new MpsatChainTask(we, settings);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        MpsatChainResultHandler monitor = new MpsatChainResultHandler(we);
        manager.queue(task, description, monitor);
        return monitor;
    }

    public boolean checkPrerequisites(WorkspaceEntry we) {
        if (isApplicableTo(we)) {
            PetriNetModel net = WorkspaceUtils.getAs(we, PetriNetModel.class);
            if (net != null) {
                return PetriUtils.checkSoundness(net, true);
            }
        }
        return false;
    }

    public abstract MpsatParameters getSettings(WorkspaceEntry we);

}
