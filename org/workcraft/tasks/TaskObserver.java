package org.workcraft.tasks;

public interface TaskObserver {
	public ProgressObserver taskStarting(String description);
}
