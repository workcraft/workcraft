package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.commands.TimeConsumingCommand;
import org.workcraft.plugins.mpsat_verification.tasks.OutputDeterminacyTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.utils.CachedVerificationUtils;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class OutputDeterminacyVerificationCommand
        extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableCommand<Boolean>, TimeConsumingCommand {

    @Override
    public Section getSection() {
        return AbstractEssentialVerificationCommand.SECTION;
    }

    @Override
    public String getDisplayName() {
        return "Output determinacy";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 80;
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
        VerificationChainResultHandlingMonitor monitor = queueTask(we, false, true);
        return monitor.waitForHandledResult();
    }

    private VerificationChainResultHandlingMonitor queueTask(WorkspaceEntry we) {
        return queueTask(we, true, true);
    }

    private VerificationChainResultHandlingMonitor queueTask(WorkspaceEntry we, boolean interactive,
            boolean reuseCachedResults) {

        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we);
        monitor.setInteractive(interactive);
        if (!isApplicableTo(we)) {
            monitor.isFinished(Result.cancel());
        } else if (!CachedVerificationUtils.tryReuse(monitor, CachedVerificationUtils.create(we, this),
                reuseCachedResults, () -> queueTask(we, true, false))) {

            OutputDeterminacyTask task = new OutputDeterminacyTask(we);
            TaskManager manager = Framework.getInstance().getTaskManager();
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            manager.queue(task, description, monitor);
        }
        return monitor;
    }

}