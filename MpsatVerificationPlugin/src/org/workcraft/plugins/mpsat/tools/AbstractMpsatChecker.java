package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.VerificationTool;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractMpsatChecker extends VerificationTool {

    @Override
    public final void run(WorkspaceEntry we) {
        final MpsatSettings settings = getSettings();
        final MpsatChainTask mpsatTask = new MpsatChainTask(we, settings);

        String description = "MPSat tool chain";
        String title = we.getTitle();
        if (!title.isEmpty()) {
            description += "(" + title + ")";
        }
        MpsatChainResultHandler monitor = new MpsatChainResultHandler(mpsatTask);
        final Framework framework = Framework.getInstance();
        framework.getTaskManager().queue(mpsatTask, description, monitor);
    }

    @Override
    public abstract String getDisplayName();

    @Override
    public abstract boolean isApplicableTo(WorkspaceEntry we);

    public abstract MpsatSettings getSettings();

}
