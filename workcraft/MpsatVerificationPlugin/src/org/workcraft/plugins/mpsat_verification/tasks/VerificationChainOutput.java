package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class VerificationChainOutput extends ChainOutput {

    private final Result<? extends MpsatOutput> mpsatResult;
    private final VerificationParameters verificationParameters;

    public VerificationChainOutput() {
        this(null, null, null, null, null);
    }

    public VerificationChainOutput(Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult,
            Result<? extends MpsatOutput> mpsatResult,
            VerificationParameters verificationParameters) {

        this(exportResult, pcompResult, punfResult, mpsatResult, verificationParameters, null);
    }

    public VerificationChainOutput(Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult,
            Result<? extends MpsatOutput> mpsatResult,
            VerificationParameters verificationParameters,
            String message) {

        super(exportResult, pcompResult, punfResult, message);
        this.mpsatResult = mpsatResult;
        this.verificationParameters = verificationParameters;
    }

    public Result<? extends MpsatOutput> getMpsatResult() {
        return mpsatResult;
    }

    public VerificationParameters getVerificationParameters() {
        return verificationParameters;
    }

    public VerificationChainOutput applyExportResult(Result<? extends ExportOutput> exportResult) {
        return new VerificationChainOutput(exportResult, getPcompResult(), getPunfResult(),
                getMpsatResult(), getVerificationParameters(), getMessage());
    }

    public VerificationChainOutput applyPcompResult(Result<? extends PcompOutput> pcompResult) {
        return new VerificationChainOutput(getExportResult(), pcompResult, getPunfResult(),
                getMpsatResult(), getVerificationParameters(), getMessage());
    }

    public VerificationChainOutput applyPunfResult(Result<? extends PunfOutput> punfResult) {
        return new VerificationChainOutput(getExportResult(), getPcompResult(), punfResult,
                getMpsatResult(), getVerificationParameters(), getMessage());
    }

    public VerificationChainOutput applyMpsatResult(Result<? extends MpsatOutput> mpsatResult) {
        return new VerificationChainOutput(getExportResult(), getPcompResult(), getPunfResult(),
                mpsatResult, getVerificationParameters(), getMessage());
    }

    public VerificationChainOutput applyVerificationParameters(VerificationParameters verificationParameters) {
        return new VerificationChainOutput(getExportResult(), getPcompResult(), getPunfResult(),
                getMpsatResult(), verificationParameters, getMessage());
    }

    public VerificationChainOutput applyMessage(String message) {
        return new VerificationChainOutput(getExportResult(), getPcompResult(), getPunfResult(),
                getMpsatResult(), getVerificationParameters(), message);
    }

}
