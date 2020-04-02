package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.tasks.Result;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;

public class ConflictResolutionChainResultHandlingMonitor
        extends AbstractChainResultHandlingMonitor<VerificationChainOutput, WorkspaceEntry> {

    private Collection<Mutex> mutexes;

    public ConflictResolutionChainResultHandlingMonitor(WorkspaceEntry we, boolean interactive) {
        super(we, interactive);
    }

    public Collection<Mutex> getMutexes() {
        return mutexes;
    }

    public void setMutexes(Collection<Mutex> mutexes) {
        this.mutexes = mutexes;
    }

    @Override
    public WorkspaceEntry handleSuccess(Result<? extends VerificationChainOutput> chainResult) {
        VerificationChainOutput chainOutput = chainResult.getPayload();
        Result<? extends VerificationOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
        VerificationOutput mpsatOutput = (mpsatResult == null) ? null : mpsatResult.getPayload();

        return new CscConflictResolutionOutputHandler(getWorkspaceEntry(), mpsatOutput,
                mutexes, isInteractive()).interpret();
    }

    @Override
    public boolean isConsistencyCheckMode(VerificationChainOutput chainOutput) {
        VerificationParameters verificationParameters = chainOutput.getVerificationParameters();
        return (verificationParameters != null)
                && (verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY);
    }

    @Override
    public Result<? extends VerificationOutput> getFailedMpsatResult(VerificationChainOutput chainOutput) {
        Result<? extends VerificationOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
        if ((mpsatResult != null) && (mpsatResult.getOutcome() == Result.Outcome.FAILURE)) {
            return mpsatResult;
        }
        return null;
    }

}
