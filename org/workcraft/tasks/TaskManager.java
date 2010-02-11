package org.workcraft.tasks;

public interface TaskManager {
	public void addObserver (TaskMonitor obs);
	public void removeObserver (TaskMonitor obs);
	public void queue (Task task, String description);
	public void queue (Task task, String description, ProgressMonitor monitor);
	public Result execute (Task task, String description);
}
