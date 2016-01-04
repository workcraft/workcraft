package org.workcraft.plugins.mpsat.tasks;

import java.util.List;

import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;

public class MpsatCombinedChainResult {
	private Result<? extends Object> exportResult;
	private Result<? extends ExternalProcessResult> pcompResult;
	private Result<? extends ExternalProcessResult> punfResult;
	private List<Result<? extends ExternalProcessResult>> mpsatResultList;
	private List<MpsatSettings> mpsatSettingsList;
	private String message;

	public MpsatCombinedChainResult(Result<? extends Object> exportResult,
			Result<? extends ExternalProcessResult> pcompResult,
			Result<? extends ExternalProcessResult> punfResult,
			List<Result<? extends ExternalProcessResult>> mpsatResultList,
			List<MpsatSettings> mpsatSettingsList, String message) {

		this.exportResult = exportResult;
		this.pcompResult = pcompResult;
		this.punfResult = punfResult;
		this.mpsatResultList = mpsatResultList;
		this.mpsatSettingsList = mpsatSettingsList;
		this.message = message;
	}

	public MpsatCombinedChainResult(Result<? extends Object> exportResult,
			Result<? extends ExternalProcessResult> pcompResult,
			Result<? extends ExternalProcessResult> punfResult,
			List<Result<? extends ExternalProcessResult>> mpsatResultList,
			List<MpsatSettings> mpsatSettingsList) {

		this(exportResult, pcompResult, punfResult, mpsatResultList, mpsatSettingsList, null);
	}

	public List<MpsatSettings> getMpsatSettingsList() {
		return mpsatSettingsList;
	}

	public Result<? extends Object> getExportResult() {
		return exportResult;
	}

	public Result<? extends ExternalProcessResult> getPcompResult() {
		return pcompResult;
	}

	public Result<? extends ExternalProcessResult> getPunfResult() {
		return punfResult;
	}

	public List<Result<? extends ExternalProcessResult>> getMpsatResultList() {
		return mpsatResultList;
	}

	public String getMessage() {
		return message;
	}

}
