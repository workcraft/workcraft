package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.tasks.Result;

public class TransformationResult {
	private Result<? extends ExternalProcessResult> petrifyResult;
	private STGModel result;

	public TransformationResult(Result<? extends ExternalProcessResult> petrifyResult, STGModel result) {
		this.petrifyResult = petrifyResult;
		this.result = result;
	}

	public  Result<? extends ExternalProcessResult> getPetrifyResult() {
		return petrifyResult;
	}

	public STGModel getResult() {
		return result;
	}
}
