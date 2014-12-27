package org.workcraft.plugins.fsm.task;

import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;

public class ConversionResult {
	private Result<? extends ExternalProcessResult> petrifyResult;
	private Fsm result;

	public ConversionResult(Result<? extends ExternalProcessResult> petrifyResult, Fsm result) {
		this.petrifyResult = petrifyResult;
		this.result = result;
	}

	public  Result<? extends ExternalProcessResult> getPetrifyResult() {
		return petrifyResult;
	}

	public Fsm getResult() {
		return result;
	}
}
