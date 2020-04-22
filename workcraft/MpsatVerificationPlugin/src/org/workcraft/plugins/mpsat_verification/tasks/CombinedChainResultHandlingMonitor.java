package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;
import org.workcraft.traces.Solution;
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
            String msg = "The following checks passed:";
            for (VerificationParameters verificationParameters : chainOutput.getVerificationParametersList()) {
                msg += "\n * " + verificationParameters.getDescription();
            }
            // No solution found in any of the MPSat tasks
            if (vacuousMutexImplementability) {
                // Add trivial mutex implementability result if no mutex places found
                msg += "\n * Mutex implementability (vacuously)";
            }

            if (isInteractive()) {
                DialogUtils.showInfo(msg);
            } else {
                LogUtils.logInfo(msg);
            }
            return true;
        }

        Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
        ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();
        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();
        VerificationParameters violationVerificationParameters = violationMpsatOutput.getVerificationParameters();

        return new VerificationOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                violationMpsatOutput, violationVerificationParameters, chainOutput.getMessage(),
                isInteractive()).interpret();
    }

    public static MpsatOutput getViolationMpsatOutput(Result<? extends CombinedChainOutput> chainResult) {
        if ((chainResult != null) && (chainResult.isSuccess())) {
            CombinedChainOutput chainOutput = chainResult.getPayload();
            for (Result<? extends MpsatOutput> mpsatResult : chainOutput.getMpsatResultList()) {
                if (mpsatResult != null) {
                    MpsatOutput mpsatOutput = mpsatResult.getPayload();
                    String mpsatStdout = mpsatOutput.getStdoutString();
                    MpsatOutputParser mdp = new MpsatOutputParser(mpsatStdout);
                    List<Solution> solutions = mdp.getSolutions();
                    if (TraceUtils.hasTraces(solutions)) {
                        return mpsatOutput;
                    }
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
