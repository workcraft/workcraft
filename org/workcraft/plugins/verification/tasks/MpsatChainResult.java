package org.workcraft.plugins.verification.tasks;

import org.workcraft.tasks.Result;

public class MpsatChainResult {
	private Result<ExternalProcessResult> punfResult;
	private Result<ExternalProcessResult> mpsatResult;
	private Result<Object> exportResult;

	public MpsatChainResult(Result<Object> exportResult,
				Result<ExternalProcessResult> punfResult,
			Result<ExternalProcessResult> mpsatResult) {
		this.punfResult = punfResult;
		this.mpsatResult = mpsatResult;
		this.exportResult = exportResult;
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