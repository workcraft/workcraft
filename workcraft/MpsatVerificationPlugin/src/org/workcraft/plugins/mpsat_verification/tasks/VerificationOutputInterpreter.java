package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.OutcomeUtils;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.AbstractOutputInterpreter;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class VerificationOutputInterpreter extends AbstractOutputInterpreter<MpsatOutput, Boolean> {

    private final ExportOutput exportOutput;
    private final PcompOutput pcompOutput;
    private final VerificationParameters verificationParameters;
    private final String chainMessage;

    VerificationOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput,
            VerificationParameters verificationParameters,
            String chainMessage, boolean interactive) {

        super(we, mpsatOutput, interactive);
        this.exportOutput = exportOutput;
        this.pcompOutput = pcompOutput;
        this.verificationParameters = verificationParameters;
        this.chainMessage = chainMessage;
    }

    @Override
    public Boolean interpret() {
        // One of the MPSat tasks returned a solution trace
        VerificationMode verificationMode = verificationParameters.getMode();
        switch (verificationMode) {
            case UNDEFINED -> {
                String message = chainMessage;
                if ((message == null) && (verificationParameters.getDescription() != null)) {
                    message = verificationParameters.getDescription();
                }
                boolean propertyHolds = verificationParameters.isInversePredicate();
                OutcomeUtils.showOutcome(propertyHolds, message, isInteractive());
                return propertyHolds;
            }
            case NORMALCY, ASSERTION, REACHABILITY, STG_REACHABILITY, STG_REACHABILITY_CONSISTENCY -> {
                return new ReachabilityOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                        getOutput(), isInteractive()).interpret();
            }
            case REACHABILITY_REDUNDANCY -> {
                return new RedundancyOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                        getOutput(), isInteractive()).interpret();
            }
            case DEADLOCK -> {
                return new DeadlockFreenessOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                        getOutput(), isInteractive()).interpret();
            }
            case STG_REACHABILITY_OUTPUT_PERSISTENCY -> {
                return new OutputPersistencyOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                        getOutput(), isInteractive()).interpret();
            }
            case STG_REACHABILITY_LOCAL_SELF_TRIGGERING -> {
                return new LocalSelfTriggeringInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                        getOutput(), isInteractive()).interpret();
            }
            case STG_REACHABILITY_DI_INTERFACE -> {
                return new DiInterfaceInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                        getOutput(), isInteractive()).interpret();
            }
            case STG_REACHABILITY_OUTPUT_DETERMINACY -> {
                return new OutputDeterminacyOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                        getOutput(), isInteractive()).interpret();
            }
            case STG_REACHABILITY_REFINEMENT -> {
                return new RefinementOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                        getOutput(), isInteractive()).interpret();
            }
            case STG_REACHABILITY_CONFORMATION -> {
                return new ConformationOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                        getOutput(), isInteractive()).interpret();
            }
            case USC_CONFLICT_DETECTION, CSC_CONFLICT_DETECTION -> {
                return new EncodingConflictOutputHandler(getWorkspaceEntry(), getOutput(),
                        isInteractive()).interpret();
            }
        }
        DialogUtils.showError(verificationMode.name() + " is not supported by MPSat verification.");
        return null;
    }

}
