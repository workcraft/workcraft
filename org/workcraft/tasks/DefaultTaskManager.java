package org.workcraft.tasks;

import java.util.ArrayList;

import org.workcraft.Framework;

public class DefaultTaskManager implements TaskManager {

	private final Framework framework;

	public DefaultTaskManager(Framework framework) {
		this.framework = framework;
	}

	TaskObserverList taskObserverList = new TaskObserverList();

	static class TaskObserverList extends ArrayList<TaskMonitor> implements TaskMonitor
	{
		private static final long serialVersionUID = 1L;

		@Override
		public <T> ProgressMonitorArray<T> taskStarting(String description) {
			ProgressMonitorArray<T> l = new ProgressMonitorArray<T>();
			for(TaskMonitor obs : this)
				l.add(obs.taskStarting(description));
			return l;
		}

	}

	@Override
	public void addObserver(TaskMonitor obs) {
		taskObserverList.add(obs);
	}


	@Override
	public <T> Result<T> execute(Task<T> task, String description) {
		return execute (task, description, null);
	}

	@Override
	public <T> Result<T> execute(Task<T> task, String description, ProgressMonitor<? super T> observer) {
		ProgressMonitorArray<T> progressMon = taskObserverList.taskStarting(description);
		if (observer != null)
			progressMon.add(observer);
		Result<T> result = task.run(progressMon);
		progressMon.finished(result, description);
		return result;
	}

	@Override
	public <T> void queue(final Task<T> task, final String description) {
		new Thread(new Runnable()
		{
			@Override
			public void run() {
				execute(task, description);
			}
		}
		).start();
	}

	@Override
	public <T> void queue(final Task<T> task, final String description, final ProgressMonitor<? super T> observer) {
		new Thread(new Runnable()
		{
			@Override
			public void run() {
				execute(task, description, observer);
			}
		}
		).start();
	}

	@Override
	public void removeObserver(TaskMonitor obs) {
		taskObserverList.remove(obs);
	}

	public Framework getFramework() {
		return framework;
	}
}
