package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.VerificationParameters.SolutionMode;
import org.workcraft.plugins.mpsat.tasks.*;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.Result;
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
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
        queueCscConflictResolution(we, monitor);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
        queueCscConflictResolution(we, monitor);

        Collection<Mutex> mutexes = monitor.getMutexes();
        Result<? extends VerificationChainOutput> chainResult = monitor.waitResult();
        VerificationChainOutput chainOutput = chainResult.getPayload();
        Result<? extends VerificationOutput> mpsatResult = chainOutput.getMpsatResult();
        VerificationOutput mpsatOutput = mpsatResult.getPayload();
        CscConflictResolutionOutputHandler resultHandler = new CscConflictResolutionOutputHandler(
                we, mpsatOutput, mutexes);

        resultHandler.run();
        return resultHandler.getResult();
    }

    private void queueCscConflictResolution(WorkspaceEntry we, VerificationChainResultHandlingMonitor monitor) {
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
