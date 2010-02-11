package org.workcraft.tasks;

public class DummyProgressMonitor implements ProgressMonitor {
	@Override
	public void finished(Result result, String description) {
	}

	@Override
	public boolean isCancelRequested() {
		return false;
	}

	@Override
	public void logMessage(String message) {
	}

	@Override
	public void progressUpdate(double completion) {
	}

	@Override
	public void logErrorMessage(String message) {
	}
}
