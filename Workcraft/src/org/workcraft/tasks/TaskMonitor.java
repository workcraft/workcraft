package org.workcraft.tasks;

public interface TaskMonitor {
	public <T> ProgressMonitor<T> taskStarting(String description);
}
