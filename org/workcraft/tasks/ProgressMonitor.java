package org.workcraft.tasks;

public interface ProgressMonitor {

	public abstract void progressUpdate(double completion);

	public abstract void logMessage(String message);

	public abstract boolean isCancelRequested();

}