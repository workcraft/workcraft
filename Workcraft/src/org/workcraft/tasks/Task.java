package org.workcraft.tasks;

public interface Task <T> {
	public Result<? extends T> run (ProgressMonitor<? super T> monitor);
}
