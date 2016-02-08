package org.workcraft.plugins.dfs.tools;

import org.workcraft.Framework;
import org.workcraft.VerificationTool;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.tasks.CheckDataflowTask;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckDataflowTool extends VerificationTool {

    public String getDisplayName() {
        return "Deadlock and hazard (reuse unfolding) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Dfs;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final CheckDataflowTask task = new CheckDataflowTask(we);
        String description = "MPSat tool chain";
        String title = we.getTitle();
        if (!title.isEmpty()) {
            description += "(" + title +")";
        }
        final Framework framework = Framework.getInstance();
        framework.getTaskManager().queue(task, description, new MpsatChainResultHandler(task));
    }

}
