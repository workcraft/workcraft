package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

import java.util.List;

public class CombinedChainOutput extends ChainOutput {

    private List<Result<? extends MpsatOutput>> mpsatResultList;
    private List<VerificationParameters> verificationParametersList;

    public CombinedChainOutput(
            Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult,
            List<Result<? extends MpsatOutput>> mpsatResultList,
            List<VerificationParameters> verificationParametersList) {

        this(exportResult, pcompResult, punfResult, mpsatResultList, verificationParametersList, null);
    }

    public CombinedChainOutput(
            Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult,
            List<Result<? extends MpsatOutput>> mpsatResultList,
            List<VerificationParameters> verificationParametersList,
            String message) {

        super(exportResult, pcompResult, punfResult, message);
        this.mpsatResultList = mpsatResultList;
        this.verificationParametersList = verificationParametersList;
    }

    public List<VerificationParameters> getVerificationParametersList() {
        return verificationParametersList;
    }

    public List<Result<? extends MpsatOutput>> getMpsatResultList() {
        return mpsatResultList;
    }

}
