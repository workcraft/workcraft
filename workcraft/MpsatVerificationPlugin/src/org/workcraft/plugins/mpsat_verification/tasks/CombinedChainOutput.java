package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

import java.util.List;

public class CombinedChainOutput extends ChainOutput {

    private final List<Result<? extends MpsatOutput>> mpsatResultList;
    private final List<VerificationParameters> verificationParametersList;
    private final String message;

    public CombinedChainOutput() {
        this(null, null, null, null, null, null);
    }

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

        super(exportResult, pcompResult, punfResult);
        this.mpsatResultList = mpsatResultList;
        this.verificationParametersList = verificationParametersList;
        this.message = message;
    }

    public List<Result<? extends MpsatOutput>> getMpsatResultList() {
        return mpsatResultList;
    }

    public List<VerificationParameters> getVerificationParametersList() {
        return verificationParametersList;
    }

    public String getMessage() {
        return message;
    }

    public CombinedChainOutput applyExportResult(Result<? extends ExportOutput> exportResult) {
        return new CombinedChainOutput(exportResult, getPcompResult(), getPunfResult(),
                getMpsatResultList(), getVerificationParametersList(), getMessage());
    }

    public CombinedChainOutput applyPcompResult(Result<? extends PcompOutput> pcompResult) {
        return new CombinedChainOutput(getExportResult(), pcompResult, getPunfResult(),
                getMpsatResultList(), getVerificationParametersList(), getMessage());
    }

    public CombinedChainOutput applyPunfResult(Result<? extends PunfOutput> punfResult) {
        return new CombinedChainOutput(getExportResult(), getPcompResult(), punfResult,
                getMpsatResultList(), getVerificationParametersList(), getMessage());
    }

    public CombinedChainOutput applyMpsatResultList(List<Result<? extends MpsatOutput>> mpsatResultList) {
        return new CombinedChainOutput(getExportResult(), getPcompResult(), getPunfResult(),
                mpsatResultList, getVerificationParametersList(), getMessage());
    }

    public CombinedChainOutput applyVerificationParametersList(List<VerificationParameters> verificationParametersList) {
        return new CombinedChainOutput(getExportResult(), getPcompResult(), getPunfResult(),
                getMpsatResultList(), verificationParametersList, getMessage());
    }

    public CombinedChainOutput applyMessage(String message) {
        return new CombinedChainOutput(getExportResult(), getPcompResult(), getPunfResult(),
                getMpsatResultList(), getVerificationParametersList(), message);
    }

}
