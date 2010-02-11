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
		public ProgressMonitorArray taskStarting(String description) {
			ProgressMonitorArray l = new ProgressMonitorArray();
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
	public Result execute(Task task, String description) {
		ProgressMonitor progressMon = taskObserverList.taskStarting(description);
		Result result = task.run(progressMon);
		progressMon.finished(result, description);
		return result;
	}

	@Override
	public void queue(final Task task, final String description) {
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
	public void queue(final Task task, final String description, final ProgressMonitor observer) {
		new Thread(new Runnable()
		{
			@Override
			public void run() {
				ProgressMonitorArray progressMon = taskObserverList.taskStarting(description);
				progressMon.add(observer);
				Result result = task.run(progressMon);
				progressMon.finished(result, description);
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
