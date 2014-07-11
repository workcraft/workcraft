package org.workcraft.plugins.cpog.tasks;

import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;

public class ProgrammerChainResult {
	private Result<? extends ExternalProcessResult> punfResult;
	private Result<? extends ExternalProcessResult> encoderResult;
	private Result<? extends Object> exportResult;
	private EncoderSettings encoderSettings;
	private String message;

	public ProgrammerChainResult(Result<? extends Object> exportResult,
				Result<? extends ExternalProcessResult> punfResult,
			Result<? extends ExternalProcessResult> encoderResult,
			EncoderSettings encoderSettings) {
		this.punfResult = punfResult;
		this.encoderResult = encoderResult;
		this.exportResult = exportResult;
		this.encoderSettings = encoderSettings;
	}

	public ProgrammerChainResult(Result<? extends Object> exportResult,
			Result<? extends ExternalProcessResult> punfResult,
		Result<? extends ExternalProcessResult> encoderResult,
		EncoderSettings encoderSettings, String message) {

		this.punfResult = punfResult;
		this.encoderResult = encoderResult;
		this.exportResult = exportResult;
		this.encoderSettings = encoderSettings;

		this.message = message;
	}

	public EncoderSettings getProgrammerSettings() {
		return encoderSettings;
	}

	public Result<? extends ExternalProcessResult> getPunfResult() {
		return punfResult;
	}

	public Result<? extends ExternalProcessResult> getEncoderResult() {
		return encoderResult;
	}

	public String getMessage() {
		return message;
	}

	public Result<? extends Object> getExportResult() {
		return exportResult;
	}
}
