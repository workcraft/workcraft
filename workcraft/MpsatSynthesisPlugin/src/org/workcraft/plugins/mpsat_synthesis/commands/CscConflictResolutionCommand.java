package org.workcraft.plugins.mpsat_synthesis.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.mpsat_synthesis.SynthesisMode;
import org.workcraft.plugins.mpsat_synthesis.tasks.SynthesisChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_synthesis.tasks.SynthesisChainTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;

public class CscConflictResolutionCommand implements ScriptableCommand<WorkspaceEntry> {

    private static final String TITLE = "Resolution of CSC conflicts";

    @Override
    public Section getSection() {
        return new Section("Encoding conflicts");
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
        queueTask(we);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        return queueTask(we).waitForHandledResult();
    }

    private SynthesisChainResultHandlingMonitor queueTask(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        SynthesisChainResultHandlingMonitor monitor = new SynthesisChainResultHandlingMonitor(we, mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        SynthesisChainTask task = new SynthesisChainTask(we, SynthesisMode.RESOLVE_ENCODING_CONFLICTS, mutexes);
        taskManager.queue(task, TITLE, monitor);
        return monitor;
    }

}
