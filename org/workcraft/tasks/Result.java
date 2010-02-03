package org.workcraft.tasks;

public interface Result {

	public enum ExitStatus {
		OK,
		CANCELLED,
		FAILED
	}

	public static final Result OK = new DefaultResult(ExitStatus.OK);
	public static final Result CANCELLED = new DefaultResult(ExitStatus.CANCELLED);
	public static final Result FAILED = new DefaultResult(ExitStatus.FAILED);

	public ExitStatus getExitStatus();
}