package org.workcraft.plugins.balsa.io;

import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STGModel;

public class StgExtractionResult {

	public StgExtractionResult(STGModel result, ExternalProcessResult pcompResult)
	{
		this.result = result;
		this.pcompResult = pcompResult;
	}

	private final STGModel result;
	private final ExternalProcessResult pcompResult;
	public STGModel getResult() {
		return result;
	}
	public ExternalProcessResult getPcompResult() {
		return pcompResult;
	}
}
