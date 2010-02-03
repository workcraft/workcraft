package org.workcraft.tasks;

public interface TaskManager {
	public void addObserver (TaskObserver obs);
	public void removeObserver (TaskObserver obs);
	public void queue (Task task, String description);
	public void queue (Task task, String description, ProgressObserver observer);
	public Result execute (Task task, String description);
}
