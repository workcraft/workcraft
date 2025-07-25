package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.OutcomeUtils;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.List;

public class CombinedChainResultHandlingMonitor extends AbstractChainResultHandlingMonitor<CombinedChainOutput> {

    private static final String MUTEX_PROTOCOL_VACUOUS_PROPERTY_DESCRIPTION = "Mutex protocol (vacuously)";

    private boolean vacuousMutexProtocol = false;

    public CombinedChainResultHandlingMonitor(WorkspaceEntry we) {
        super(we);
    }

    public void setVacuousMutexProtocol(boolean value) {
        vacuousMutexProtocol = value;
    }

    @Override
    public Boolean handleSuccess(Result<? extends CombinedChainOutput> chainResult) {
        CombinedChainOutput chainOutput = chainResult.getPayload();
        List<Result<? extends MpsatOutput>> mpsatResultList = chainOutput.getMpsatResultList();

        MpsatOutput violationMpsatOutput = null;
        VerificationParameters violationVerificationParameters = null;
        if (mpsatResultList == null) {
            List<VerificationParameters> verificationParametersList = chainOutput.getVerificationParametersList();
            if (verificationParametersList.size() == 1) {
                // Property violated for a trivial case (therefore no mpsatResult, but a single verificationParameters)
                violationVerificationParameters = verificationParametersList.iterator().next();
            }
        } else {
            violationMpsatOutput = getViolationMpsatOutput(mpsatResultList);
            if (violationMpsatOutput == null) {
                displayChecksPassedMessage(chainOutput);
                return true;
            }
            violationVerificationParameters = violationMpsatOutput.getVerificationParameters();
        }

        Result<? extends ExportOutput> exportResult = chainOutput.getExportResult();
        ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();

        Result<? extends PcompOutput> pcompResult = chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();

        String message = chainOutput.getMessage();

        return new VerificationOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                violationMpsatOutput, violationVerificationParameters, message,
                isInteractive()).interpret();
    }

    private void displayChecksPassedMessage(CombinedChainOutput chainOutput) {
        StringBuilder msg = new StringBuilder();
        String chainOutputMessage = chainOutput.getMessage();
        if (chainOutputMessage != null) {
            msg.append(chainOutputMessage);
        } else {
            List<VerificationParameters> verificationParametersList = chainOutput.getVerificationParametersList();
            List<VerificationParameters> remainingVerificationParametersList = new ArrayList<>(verificationParametersList);
            // Start with Consistency property
            for (VerificationParameters verificationParameters : verificationParametersList) {
                String propertyDescription = verificationParameters.getDescription();
                if ((verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY)
                        || (ReachUtils.CONSISTENCY_PROPERTY_DESCRIPTION.equals(propertyDescription))) {

                    msg.append(TextUtils.getBulletpoint(propertyDescription));
                    remainingVerificationParametersList.remove(verificationParameters);
                    break;
                }
            }
            // Then process Output determinacy
            for (VerificationParameters verificationParameters : verificationParametersList) {
                String propertyDescription = verificationParameters.getDescription();
                if ((verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_OUTPUT_DETERMINACY)
                        || (OutputDeterminacyTask.VACUOUS_PROPERTY_DESCRIPTION.equals(propertyDescription))) {

                    msg.append(TextUtils.getBulletpoint(propertyDescription));
                    remainingVerificationParametersList.remove(verificationParameters);
                    break;
                }
            }
            // Finally process the remaining properties and handle vacuous mutex protocol specially
            boolean needToInsertVacuousMutexProtocol = vacuousMutexProtocol;
            for (VerificationParameters verificationParameters : remainingVerificationParametersList) {
                // Insert Mutex protocol (vacuously) before Output persistency if no mutex places found
                if (needToInsertVacuousMutexProtocol
                        && (verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_OUTPUT_PERSISTENCY)) {

                    msg.append(TextUtils.getBulletpoint(MUTEX_PROTOCOL_VACUOUS_PROPERTY_DESCRIPTION));
                    needToInsertVacuousMutexProtocol = false;
                }
                msg.append(TextUtils.getBulletpoint(verificationParameters.getDescription()));
            }
            // Add Mutex protocol (vacuous) in the end, if needed and no Output persistence was checked
            if (needToInsertVacuousMutexProtocol) {
                msg.append(TextUtils.getBulletpoint(MUTEX_PROTOCOL_VACUOUS_PROPERTY_DESCRIPTION));
            }
            if (msg.isEmpty()) {
                msg.append("All properties have been successfully verified.");
            } else {
                msg.insert(0, "The following properties have been successfully verified:");
            }
        }
        if (isInteractive()) {
            DialogUtils.showInfo(msg.toString(), OutcomeUtils.TITLE);
        } else {
            LogUtils.logInfo(msg.toString());
        }
    }

    public static MpsatOutput getViolationMpsatOutput(CombinedChainOutput chainOutput) {
        List<Result<? extends MpsatOutput>> mpsatResultList = chainOutput.getMpsatResultList();
        if (mpsatResultList != null) {
            return getViolationMpsatOutput(mpsatResultList);
        }
        return null;
    }

    private static MpsatOutput getViolationMpsatOutput(List<Result<? extends MpsatOutput>> mpsatResultList) {
        for (Result<? extends MpsatOutput> mpsatResult : mpsatResultList) {
            if (mpsatResult != null) {
                MpsatOutput mpsatOutput = mpsatResult.getPayload();
                boolean inversePredicate = mpsatOutput.getVerificationParameters().isInversePredicate();
                if (mpsatOutput.hasSolutions() == inversePredicate) {
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
    public boolean canProcessSolution() {
        return true;
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
