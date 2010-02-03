package org.workcraft.tasks;

public interface ProgressObserver extends ProgressMonitor {
	public void finished(Result result);
}
