package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.VerificationParameters.SolutionMode;
import org.workcraft.plugins.mpsat.tasks.ConflictResolutionChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat.tasks.VerificationChainTask;
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
        ConflictResolutionChainResultHandlingMonitor monitor = new ConflictResolutionChainResultHandlingMonitor(we, true);
        queueCscConflictResolution(we, monitor);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        ConflictResolutionChainResultHandlingMonitor monitor = new ConflictResolutionChainResultHandlingMonitor(we, true);
        queueCscConflictResolution(we, monitor);
        return monitor.waitForHandledResult();
    }

    private void queueCscConflictResolution(WorkspaceEntry we, ConflictResolutionChainResultHandlingMonitor monitor) {
        VerificationParameters verificationParameters = new VerificationParameters(TITLE,
                VerificationMode.RESOLVE_ENCODING_CONFLICTS, 4, SolutionMode.MINIMUM_COST, 1);

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        monitor.setMutexes(mutexes);
        VerificationChainTask task = new VerificationChainTask(we, verificationParameters, mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        taskManager.queue(task, TITLE, monitor);
    }

}
