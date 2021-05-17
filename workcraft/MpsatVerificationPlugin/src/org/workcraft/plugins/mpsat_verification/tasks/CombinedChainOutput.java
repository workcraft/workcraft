package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
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
            Result<? extends MpsatOutput> mpsatUnfoldingResult,
            List<Result<? extends MpsatOutput>> mpsatResultList,
            List<VerificationParameters> verificationParametersList) {

        this(exportResult, pcompResult, mpsatUnfoldingResult, mpsatResultList, verificationParametersList, null);
    }

    public CombinedChainOutput(
            Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends MpsatOutput> mpsatUnfoldingResult,
            List<Result<? extends MpsatOutput>> mpsatResultList,
            List<VerificationParameters> verificationParametersList,
            String message) {

        super(exportResult, pcompResult, mpsatUnfoldingResult);
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
        return new CombinedChainOutput(exportResult, getPcompResult(), getMpsatResult(),
                getMpsatResultList(), getVerificationParametersList(), getMessage());
    }

    public CombinedChainOutput applyPcompResult(Result<? extends PcompOutput> pcompResult) {
        return new CombinedChainOutput(getExportResult(), pcompResult, getMpsatResult(),
                getMpsatResultList(), getVerificationParametersList(), getMessage());
    }

    public CombinedChainOutput applyMpsatResult(Result<? extends MpsatOutput> mpsatResult) {
        return new CombinedChainOutput(getExportResult(), getPcompResult(), mpsatResult,
                getMpsatResultList(), getVerificationParametersList(), getMessage());
    }

    public CombinedChainOutput applyMpsatResultList(List<Result<? extends MpsatOutput>> mpsatResultList) {
        return new CombinedChainOutput(getExportResult(), getPcompResult(), getMpsatResult(),
                mpsatResultList, getVerificationParametersList(), getMessage());
    }

    public CombinedChainOutput applyVerificationParametersList(List<VerificationParameters> verificationParametersList) {
        return new CombinedChainOutput(getExportResult(), getPcompResult(), getMpsatResult(),
                getMpsatResultList(), verificationParametersList, getMessage());
    }

    public CombinedChainOutput applyMessage(String message) {
        return new CombinedChainOutput(getExportResult(), getPcompResult(), getMpsatResult(),
                getMpsatResultList(), getVerificationParametersList(), message);
    }

}
