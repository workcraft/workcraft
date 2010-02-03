package org.workcraft.tasks;

import java.util.ArrayList;

import org.workcraft.Framework;

public class DefaultTaskManager implements TaskManager {

	private final Framework framework;

	public DefaultTaskManager(Framework framework) {
		this.framework = framework;
	}

	TaskObserverList taskObserverList = new TaskObserverList();

	static class TaskObserverList extends ArrayList<TaskObserver> implements TaskObserver
	{
		private static final long serialVersionUID = 1L;

		@Override
		public ProgressObserverList taskStarting(String description) {
			ProgressObserverList l = new ProgressObserverList();
			for(TaskObserver obs : this)
				l.add(obs.taskStarting(description));
			return l;
		}

	}

	@Override
	public void addObserver(TaskObserver obs) {
		taskObserverList.add(obs);
	}

	@Override
	public Result execute(Task task, String description) {
		ProgressObserver progressMon = taskObserverList.taskStarting(description);
		Result result = task.run(progressMon);
		progressMon.finished(result);
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
	public void queue(final Task task, final String description, final ProgressObserver observer) {
		new Thread(new Runnable()
		{
			@Override
			public void run() {
				ProgressObserverList progressMon = taskObserverList.taskStarting(description);
				progressMon.add(observer);
				Result result = task.run(progressMon);
				progressMon.finished(result);
			}
		}
		).start();
	}

	@Override
	public void removeObserver(TaskObserver obs) {
		taskObserverList.remove(obs);
	}

	public Framework getFramework() {
		return framework;
	}
}
