package org.workcraft.tasks;


public class ExceptionResult implements Result {
	Throwable reason;

	public ExceptionResult(Throwable reason) {
		this.reason = reason;
	}

	@Override
	public ExitStatus getExitStatus() {
		return ExitStatus.FAILED;
	}

	public Throwable getReason() {
		return reason;
	}
}
