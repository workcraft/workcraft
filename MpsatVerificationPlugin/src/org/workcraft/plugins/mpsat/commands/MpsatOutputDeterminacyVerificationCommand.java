package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.tasks.MpsatChainOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatOutputDeterminacyTask;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatOutputDeterminacyVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Output determinacy [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM_MIDDLE;
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
        if (!isApplicableTo(we)) {
            return null;
        }
        StgModel stg = WorkspaceUtils.getAs(we, StgModel.class);
        if (!stg.getSignalReferences(Signal.Type.INTERNAL).isEmpty()) {
            String msg = "Output determinacy of STGs with internal signals may be ambiguous.\n" +
                    " Proceed anyway and interpret internal signal as dummies?";
            if (!DialogUtils.showConfirmWarning(msg, "Verification", true)) {
                return null;
            }
        }
        final Framework framework = Framework.getInstance();
        MpsatOutputDeterminacyTask task = new MpsatOutputDeterminacyTask(we);
        TaskManager manager = framework.getTaskManager();
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        MpsatChainResultHandler monitor = new MpsatChainResultHandler(we);
        manager.queue(task, description, monitor);
        return monitor;
    }

}