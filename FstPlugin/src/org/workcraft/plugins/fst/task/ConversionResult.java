package org.workcraft.plugins.fst.task;

import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;

public class ConversionResult {
	private Result<? extends ExternalProcessResult> petrifyResult;
	private Fst result;

	public ConversionResult(Result<? extends ExternalProcessResult> petrifyResult, Fst result) {
		this.petrifyResult = petrifyResult;
		this.result = result;
	}

	public  Result<? extends ExternalProcessResult> getPetrifyResult() {
		return petrifyResult;
	}

	public Fst getResult() {
		return result;
	}
}
