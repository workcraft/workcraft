package org.workcraft.tasks;

public interface ProgressMonitor {
	public void progressUpdate(double completion);
	public void logMessage(String message);
	public void logErrorMessage(String message);
	public boolean isCancelRequested();
	public void finished(Result result, String description);
}