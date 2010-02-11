package org.workcraft.tasks;

public class ExternalProcessResult implements Result {
	private int returnCode;
	private boolean userCancelled;

	public ExternalProcessResult(int returnCode, boolean userCancelled) {
		this.returnCode = returnCode;
		this.userCancelled = userCancelled;
	}

	@Override
	public ExitStatus getExitStatus() {
		if (userCancelled)
			return ExitStatus.CANCELLED;
		if (returnCode == 0)
			return ExitStatus.OK;
		return ExitStatus.FAILED;
	}

	public int getReturnCode() {
		return returnCode;
	}
}
