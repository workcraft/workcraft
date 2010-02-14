package org.workcraft.plugins.verification;

import org.workcraft.plugins.verification.tasks.ExternalProcessResult;

public abstract class MpsatResultParser {
	protected ExternalProcessResult result;

	public MpsatResultParser(ExternalProcessResult result) {
		this.result = result;
	}
}
