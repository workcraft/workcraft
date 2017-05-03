package org.workcraft.plugins.mpsat.commands;

import java.util.Collection;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatParameters.SolutionMode;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatCscConflictResolutionCommand implements Command {

    @Override
    public String getSection() {
        return "Encoding conflicts";
    }

    @Override
    public String getDisplayName() {
        return "Resolve CSC conflicts [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        MpsatParameters settings = new MpsatParameters("Resolution of CSC conflicts",
                MpsatMode.RESOLVE_ENCODING_CONFLICTS, 4, SolutionMode.MINIMUM_COST, 1);

        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final MpsatChainTask mpsatTask = new MpsatChainTask(we, settings);
        Collection<Mutex> mutexes = MutexUtils.getMutexes(WorkspaceUtils.getAs(we, Stg.class));
        final MpsatChainResultHandler monitor = new MpsatChainResultHandler(mpsatTask, mutexes);
        taskManager.queue(mpsatTask, "Resolution of CSC conflicts", monitor);
    }

}
