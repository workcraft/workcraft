package org.workcraft.plugins.mpsat.tasks;

import java.util.List;

import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class CombinedChainOutput {
    private Result<? extends ExportOutput> exportResult;
    private Result<? extends PcompOutput> pcompResult;
    private Result<? extends PunfOutput> punfResult;
    private List<Result<? extends VerificationOutput>> mpsatResultList;
    private List<VerificationParameters> verificationParametersList;
    private String message;

    public CombinedChainOutput(
            Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult,
            List<Result<? extends VerificationOutput>> mpsatResultList,
            List<VerificationParameters> verificationParametersList, String message) {

        this.exportResult = exportResult;
        this.pcompResult = pcompResult;
        this.punfResult = punfResult;
        this.mpsatResultList = mpsatResultList;
        this.verificationParametersList = verificationParametersList;
        this.message = message;
    }

    public CombinedChainOutput(
            Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult,
            List<Result<? extends VerificationOutput>> mpsatResultList,
            List<VerificationParameters> verificationParametersList) {

        this(exportResult, pcompResult, punfResult, mpsatResultList, verificationParametersList, null);
    }

    public List<VerificationParameters> getVerificationParametersList() {
        return verificationParametersList;
    }

    public Result<? extends ExportOutput> getExportResult() {
        return exportResult;
    }

    public Result<? extends PcompOutput> getPcompResult() {
        return pcompResult;
    }

    public Result<? extends PunfOutput> getPunfResult() {
        return punfResult;
    }

    public List<Result<? extends VerificationOutput>> getMpsatResultList() {
        return mpsatResultList;
    }

    public String getMessage() {
        return message;
    }

}
