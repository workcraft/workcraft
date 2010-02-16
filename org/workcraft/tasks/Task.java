package org.workcraft.tasks;

public interface Task <T> {
	public Result<T> run (ProgressMonitor<T> monitor);
}
