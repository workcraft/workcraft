package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;

public class MpsatChainResult {
	private Result<? extends ExternalProcessResult> punfResult;
	private Result<? extends ExternalProcessResult> mpsatResult;
	private Result<? extends Object> exportResult;
	private MpsatSettings mpsatSettings;

	public MpsatChainResult(Result<? extends Object> exportResult,
				Result<? extends ExternalProcessResult> punfResult,
			Result<? extends ExternalProcessResult> mpsatResult,
			MpsatSettings mpsatSettings) {
		this.punfResult = punfResult;
		this.mpsatResult = mpsatResult;
		this.exportResult = exportResult;
		this.mpsatSettings = mpsatSettings;
	}

	public MpsatSettings getMpsatSettings() {
		return mpsatSettings;
	}

	public Result<? extends ExternalProcessResult> getPunfResult() {
		return punfResult;
	}

	public Result<? extends ExternalProcessResult> getMpsatResult() {
		return mpsatResult;
	}

	public Result<? extends Object> getExportResult() {
		return exportResult;
	}
}