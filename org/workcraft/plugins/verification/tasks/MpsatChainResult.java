package org.workcraft.plugins.verification.tasks;

import org.workcraft.plugins.verification.MpsatMode;
import org.workcraft.tasks.Result;

public class MpsatChainResult {
	private Result<ExternalProcessResult> punfResult;
	private Result<ExternalProcessResult> mpsatResult;
	private Result<Object> exportResult;
	private MpsatMode mpsatMode;

	public MpsatChainResult(Result<Object> exportResult,
				Result<ExternalProcessResult> punfResult,
			Result<ExternalProcessResult> mpsatResult,
			MpsatMode mpsatMode) {
		this.punfResult = punfResult;
		this.mpsatResult = mpsatResult;
		this.exportResult = exportResult;
		this.mpsatMode = mpsatMode;
	}

	public MpsatMode getMpsatMode() {
		return mpsatMode;
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