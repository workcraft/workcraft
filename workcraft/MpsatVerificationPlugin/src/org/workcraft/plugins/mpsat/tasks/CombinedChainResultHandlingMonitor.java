package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.traces.Solution;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.TraceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.List;

public class CombinedChainResultHandlingMonitor extends AbstractChainResultHandlingMonitor<CombinedChainOutput, Boolean> {

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
        List<Result<? extends VerificationOutput>> mpsatResultList = chainOutput.getMpsatResultList();
        List<VerificationParameters> verificationParametersList = chainOutput.getVerificationParametersList();

        VerificationOutput violationMpsatOutput = null;
        VerificationParameters violationVerificationParameters = null;
        String verifiedMessageDetailes = "";
        for (int index = 0; index < mpsatResultList.size(); ++index) {
            VerificationParameters verificationParameters = verificationParametersList.get(index);
            Result<? extends VerificationOutput> mpsatResult = mpsatResultList.get(index);
            boolean hasSolutions = false;
            if (mpsatResult != null) {
                String mpsatStdout = mpsatResult.getPayload().getStdoutString();
                VerificationOutputParser mdp = new VerificationOutputParser(mpsatStdout);
                List<Solution> solutions = mdp.getSolutions();
                hasSolutions = TraceUtils.hasTraces(solutions);
            }
            if (!hasSolutions) {
                verifiedMessageDetailes += "\n * " + verificationParameters.getName();
            } else {
                violationMpsatOutput = mpsatResult.getPayload();
                violationVerificationParameters = verificationParameters;
            }
        }

        if (violationVerificationParameters == null) {
            // No solution found in any of the MPSat tasks
            if (vacuousMutexImplementability) {
                // Add trivial mutex implementability result if no mutex places found
                verifiedMessageDetailes += "\n * Mutex implementability (vacuously)";
            }
            DialogUtils.showInfo(verifiedMessageDetailes.isEmpty() ? chainOutput.getMessage()
                    : "The following checks passed:" + verifiedMessageDetailes);

            return true;
        }

        Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
        ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();
        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();

        return new VerificationOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                violationMpsatOutput, violationVerificationParameters, chainOutput.getMessage(),
                isInteractive()).interpret();
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
    public Result<? extends VerificationOutput> getFailedMpsatResult(CombinedChainOutput chainOutput) {
        List<Result<? extends VerificationOutput>> mpsatResultList = (chainOutput == null) ? null : chainOutput.getMpsatResultList();
        if (mpsatResultList != null) {
            for (Result<? extends VerificationOutput> mpsatResult : mpsatResultList) {
                if ((mpsatResult != null) && (mpsatResult.getOutcome() == Outcome.FAILURE)) {
                    return mpsatResult;
                }
            }
        }
        return null;
    }

}
