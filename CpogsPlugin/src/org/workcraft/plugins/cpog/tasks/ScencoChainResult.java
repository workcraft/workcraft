package org.workcraft.plugins.cpog.tasks;

import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;

public class ScencoChainResult {
	private Result<? extends ExternalProcessResult> scencoResult;
	private Result<? extends Object> exportResult;
	private EncoderSettings scencoSettings;
	private String message;

	public ScencoChainResult(Result<? extends Object> exportResult,
			Result<? extends ExternalProcessResult> encoderResult,
			EncoderSettings encoderSettings) {
		this.scencoResult = encoderResult;
		this.exportResult = exportResult;
		this.scencoSettings = encoderSettings;
	}

	public ScencoChainResult(Result<? extends Object> exportResult,
			Result<? extends ExternalProcessResult> punfResult,
		Result<? extends ExternalProcessResult> encoderResult,
		EncoderSettings encoderSettings, String message) {

		this.scencoResult = encoderResult;
		this.exportResult = exportResult;
		this.scencoSettings = encoderSettings;

		this.message = message;
	}

	public EncoderSettings getScencoSettings() {
		return scencoSettings;
	}

	public Result<? extends ExternalProcessResult> getEncoderResult() {
		return scencoResult;
	}

	public String getMessage() {
		return message;
	}

	public Result<? extends Object> getExportResult() {
		return exportResult;
	}
}
