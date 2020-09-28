package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TraceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.List;

public class CombinedChainResultHandlingMonitor extends AbstractChainResultHandlingMonitor<CombinedChainOutput> {

    private boolean vacuousMutexImplementability = false;

    public CombinedChainResultHandlingMonitor(WorkspaceEntry we, boolean interactive) {
        super(we, interactive);
    }

    public void setVacuousMutexImplementability(boolean value) {
        vacuousMutexImplementability = value;
    }

    @Override
    public Boolean handleSuccess(Result<? extends CombinedChainOutput> chainResult) {
        CombinedChainOutput chainOutput = chainResult.getPayload();
        MpsatOutput violationMpsatOutput = getViolationMpsatOutput(chainResult);

        if (violationMpsatOutput == null) {
            // No solution found in any of the MPSat tasks
            StringBuilder msg = new StringBuilder();
            for (VerificationParameters verificationParameters : chainOutput.getVerificationParametersList()) {
                msg.append("\n * ").append(verificationParameters.getDescription());
            }
            if (vacuousMutexImplementability) {
                // Add trivial mutex implementability result if no mutex places found
                msg.append("\n * Mutex implementability (vacuously)");
            }
            if (msg.length() == 0) {
                msg.append(chainOutput.getMessage());
            } else {
                msg.insert(0, "The following checks passed:");
            }

            if (isInteractive()) {
                DialogUtils.showInfo(msg.toString());
            } else {
                LogUtils.logInfo(msg.toString());
            }
            return true;
        }

        Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
        ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();

        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();

        VerificationParameters violationVerificationParameters = violationMpsatOutput.getVerificationParameters();

        String message = (chainOutput == null) ? null : chainOutput.getMessage();

        return new VerificationOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                violationMpsatOutput, violationVerificationParameters, message,
                isInteractive()).interpret();
    }

    public static MpsatOutput getViolationMpsatOutput(Result<? extends CombinedChainOutput> chainResult) {
        if ((chainResult != null) && (chainResult.isSuccess())) {
            return getViolationMpsatOutput(chainResult.getPayload());
        }
        return null;
    }

    public static MpsatOutput getViolationMpsatOutput(CombinedChainOutput chainOutput) {
        for (Result<? extends MpsatOutput> mpsatResult : chainOutput.getMpsatResultList()) {
            if (mpsatResult != null) {
                MpsatOutput mpsatOutput = mpsatResult.getPayload();
                if (TraceUtils.hasTraces(mpsatOutput.getSolutions())) {
                    return mpsatOutput;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isConsistencyCheckMode(CombinedChainOutput chainOutput) {
        for (VerificationParameters verificationParameters : chainOutput.getVerificationParametersList()) {
            if (verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Result<? extends MpsatOutput> getFailedMpsatResult(CombinedChainOutput chainOutput) {
        List<Result<? extends MpsatOutput>> mpsatResultList = (chainOutput == null) ? null : chainOutput.getMpsatResultList();
        if (mpsatResultList != null) {
            for (Result<? extends MpsatOutput> mpsatResult : mpsatResultList) {
                if ((mpsatResult != null) && (mpsatResult.isFailure())) {
                    return mpsatResult;
                }
            }
        }
        return null;
    }

}
