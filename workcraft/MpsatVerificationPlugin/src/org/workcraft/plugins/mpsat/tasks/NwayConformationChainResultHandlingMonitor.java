package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.List;

public class NwayConformationChainResultHandlingMonitor
        extends AbstractChainResultHandlingMonitor<VerificationChainOutput, Boolean> {

    private final ArrayList<WorkspaceEntry> wes = new ArrayList<>();

    public NwayConformationChainResultHandlingMonitor(List<WorkspaceEntry> wes) {
        super(wes.get(0), true);
        this.wes.addAll(wes);
    }

    @Override
    public Boolean handleSuccess(Result<? extends VerificationChainOutput> chainResult) {
        VerificationChainOutput chainOutput = chainResult.getPayload();
        Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
        ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();
        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();
        Result<? extends VerificationOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
        VerificationOutput mpsatOutput = (mpsatResult == null) ? null : mpsatResult.getPayload();

        return new NwayConformationOutputInterpreter(wes, exportOutput, pcompOutput,
                mpsatOutput, isInteractive()).interpret();
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
