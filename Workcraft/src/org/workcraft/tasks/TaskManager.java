package org.workcraft.tasks;

public interface TaskManager {
	public void addObserver (TaskMonitor obs);
	public void removeObserver (TaskMonitor obs);
	public <T> void queue (Task<T> task, String description);
	public <T> void queue (Task<T> task, String description, ProgressMonitor<? super T> monitor);
	public <T> Result<? extends T> execute (Task<T> task, String description);
	public <T> Result<? extends T> execute (Task<T> task, String description, ProgressMonitor<? super T> monitor);
}
