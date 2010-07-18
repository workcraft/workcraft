package org.workcraft.plugins.shared.tasks;

import org.workcraft.plugins.shared.MpsatSettings;
import org.workcraft.tasks.Result;

public class MpsatChainResult {
	private Result<ExternalProcessResult> punfResult;
	private Result<ExternalProcessResult> mpsatResult;
	private Result<Object> exportResult;
	private MpsatSettings mpsatSettings;

	public MpsatChainResult(Result<Object> exportResult,
				Result<ExternalProcessResult> punfResult,
			Result<ExternalProcessResult> mpsatResult,
			MpsatSettings mpsatSettings) {
		this.punfResult = punfResult;
		this.mpsatResult = mpsatResult;
		this.exportResult = exportResult;
		this.mpsatSettings = mpsatSettings;
	}

	public MpsatSettings getMpsatSettings() {
		return mpsatSettings;
	}

	public Result<ExternalProcessResult> getPunfResult() {
		return punfResult;
	}

	public Result<ExternalProcessResult> getMpsatResult() {
		return mpsatResult;
	}

	public Result<Object> getExportResult() {
		return exportResult;
	}
}