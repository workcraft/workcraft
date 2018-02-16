package org.workcraft.plugins.mpsat.tasks;

import java.util.List;

import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;

public class MpsatCombinedChainResult {
    private Result<? extends ExportOutput> exportResult;
    private Result<? extends PcompOutput> pcompResult;
    private Result<? extends PunfOutput> punfResult;
    private List<Result<? extends ExternalProcessOutput>> mpsatResultList;
    private List<MpsatParameters> mpsatSettingsList;
    private String message;

    public MpsatCombinedChainResult(
            Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult,
            List<Result<? extends ExternalProcessOutput>> mpsatResultList,
            List<MpsatParameters> mpsatSettingsList, String message) {

        this.exportResult = exportResult;
        this.pcompResult = pcompResult;
        this.punfResult = punfResult;
        this.mpsatResultList = mpsatResultList;
        this.mpsatSettingsList = mpsatSettingsList;
        this.message = message;
    }

    public MpsatCombinedChainResult(
            Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult,
            List<Result<? extends ExternalProcessOutput>> mpsatResultList,
            List<MpsatParameters> mpsatSettingsList) {

        this(exportResult, pcompResult, punfResult, mpsatResultList, mpsatSettingsList, null);
    }

    public List<MpsatParameters> getMpsatSettingsList() {
        return mpsatSettingsList;
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

    public List<Result<? extends ExternalProcessOutput>> getMpsatResultList() {
        return mpsatResultList;
    }

    public String getMessage() {
        return message;
    }

}
