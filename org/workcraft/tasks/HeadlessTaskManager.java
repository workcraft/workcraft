package org.workcraft.tasks;

public class HeadlessTaskManager implements TaskManager {

	@Override
	public void addObserver(TaskMonitor obs) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public void removeObserver(TaskMonitor obs) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public <T> void queue(Task<T> task, String description) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public <T> void queue(Task<T> task, String description,
			ProgressMonitor<? super T> monitor) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public <T> Result<T> execute(Task<T> task, String description) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public <T> Result<T> execute(Task<T> task, String description,
			ProgressMonitor<? super T> monitor) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

}
