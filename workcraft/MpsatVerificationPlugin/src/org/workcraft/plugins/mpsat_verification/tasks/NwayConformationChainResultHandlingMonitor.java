package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.List;

public class NwayConformationChainResultHandlingMonitor
        extends AbstractChainResultHandlingMonitor<VerificationChainOutput> {

    private final ArrayList<WorkspaceEntry> wes = new ArrayList<>();

    public NwayConformationChainResultHandlingMonitor(List<WorkspaceEntry> wes) {
        super(wes.isEmpty() ? null : wes.get(0));
        this.wes.addAll(wes);
    }

    @Override
    public Boolean handleSuccess(Result<? extends VerificationChainOutput> chainResult) {
        VerificationChainOutput chainOutput = chainResult.getPayload();
        Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
        ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();

        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();

        Result<? extends MpsatOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
        MpsatOutput mpsatOutput = (mpsatResult == null) ? null : mpsatResult.getPayload();

        return getInterpreter(exportOutput, pcompOutput, mpsatOutput).interpret();
    }

    public ConformationOutputInterpreter getInterpreter(ExportOutput exportOutput, PcompOutput pcompOutput,
            MpsatOutput mpsatOutput) {

        return new NwayConformationOutputInterpreter(wes, exportOutput, pcompOutput, mpsatOutput, isInteractive());
    }

    @Override
    public boolean isConsistencyCheckMode(VerificationChainOutput chainOutput) {
        VerificationParameters verificationParameters = chainOutput.getVerificationParameters();
        return (verificationParameters != null)
                && (verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY);
    }

    @Override
    public boolean canProcessSolution() {
        return false;
    }

    @Override
    public Result<? extends MpsatOutput> getFailedMpsatResult(VerificationChainOutput chainOutput) {
        Result<? extends MpsatOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
        if ((mpsatResult != null) && (mpsatResult.isFailure())) {
            return mpsatResult;
        }
        return null;
    }

}
