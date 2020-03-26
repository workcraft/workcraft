package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.mpsat.tasks.OutputDeterminacyTask;
import org.workcraft.plugins.mpsat.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandler;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class OutputDeterminacyVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

    @Override
    public String getDisplayName() {
        return "Output determinacy [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public int getPriority() {
        return 2;
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
        VerificationChainResultHandler monitor = queueVerification(we);
        Result<? extends VerificationChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getChainOutcome(result);
    }

    private VerificationChainResultHandler queueVerification(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        final Framework framework = Framework.getInstance();
        OutputDeterminacyTask task = new OutputDeterminacyTask(we);
        TaskManager manager = framework.getTaskManager();
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        VerificationChainResultHandler monitor = new VerificationChainResultHandler(we);
        manager.queue(task, description, monitor);
        return monitor;
    }

}