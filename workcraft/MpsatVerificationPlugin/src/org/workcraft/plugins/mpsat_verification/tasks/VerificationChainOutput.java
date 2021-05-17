package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class VerificationChainOutput extends ChainOutput {

    private final VerificationParameters verificationParameters;
    private final String message;

    public VerificationChainOutput() {
        this(null, null, null, null, null);
    }

    public VerificationChainOutput(Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends MpsatOutput> mpsatResult,
            VerificationParameters verificationParameters) {

        this(exportResult, pcompResult, mpsatResult, verificationParameters, null);
    }

    public VerificationChainOutput(Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends MpsatOutput> mpsatResult,
            VerificationParameters verificationParameters,
            String message) {

        super(exportResult, pcompResult, mpsatResult);
        this.verificationParameters = verificationParameters;
        this.message = message;
    }

    public VerificationParameters getVerificationParameters() {
        return verificationParameters;
    }

    public String getMessage() {
        return message;
    }

    public VerificationChainOutput applyExportResult(Result<? extends ExportOutput> exportResult) {
        return new VerificationChainOutput(exportResult, getPcompResult(), getMpsatResult(),
                getVerificationParameters(), getMessage());
    }

    public VerificationChainOutput applyPcompResult(Result<? extends PcompOutput> pcompResult) {
        return new VerificationChainOutput(getExportResult(), pcompResult, getMpsatResult(),
                getVerificationParameters(), getMessage());
    }

    public VerificationChainOutput applyMpsatResult(Result<? extends MpsatOutput> mpsatResult) {
        return new VerificationChainOutput(getExportResult(), getPcompResult(), mpsatResult,
                getVerificationParameters(), getMessage());
    }

    public VerificationChainOutput applyVerificationParameters(VerificationParameters verificationParameters) {
        return new VerificationChainOutput(getExportResult(), getPcompResult(), getMpsatResult(),
                verificationParameters, getMessage());
    }

    public VerificationChainOutput applyMessage(String message) {
        return new VerificationChainOutput(getExportResult(), getPcompResult(), getMpsatResult(),
                getVerificationParameters(), message);
    }

}
