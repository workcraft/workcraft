package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class VerificationChainOutput extends ChainOutput {

    private final Result<? extends VerificationOutput> mpsatResult;
    private final VerificationParameters verificationParameters;

    public VerificationChainOutput(Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult,
            Result<? extends VerificationOutput> mpsatResult,
            VerificationParameters verificationParameters) {

        this(exportResult, pcompResult, punfResult, mpsatResult, verificationParameters, null);
    }

    public VerificationChainOutput(Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult,
            Result<? extends VerificationOutput> mpsatResult,
            VerificationParameters verificationParameters,
            String message) {

        super(exportResult, pcompResult, punfResult, message);
        this.mpsatResult = mpsatResult;
        this.verificationParameters = verificationParameters;
    }

    public VerificationParameters getVerificationParameters() {
        return verificationParameters;
    }

    public Result<? extends VerificationOutput> getMpsatResult() {
        return mpsatResult;
    }

}
