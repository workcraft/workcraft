package org.workcraft.tasks;

public class DefaultResult implements Result {
	private ExitStatus result;

	public DefaultResult(ExitStatus result) {
		this.result = result;
	}

	@Override
	public ExitStatus getExitStatus() {
		return result;
	}
}
