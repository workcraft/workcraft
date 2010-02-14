package org.workcraft.tasks;

public class DummyProgressMonitor<T> implements ProgressMonitor<T> {
	@Override
	public void finished(Result<? extends T> result, String description) {
	}

	@Override
	public boolean isCancelRequested() {
		return false;
	}

	@Override
	public void stdout(byte[] data) {
	}

	@Override
	public void progressUpdate(double completion) {
	}

	@Override
	public void stderr(byte[] data) {
	}
}
