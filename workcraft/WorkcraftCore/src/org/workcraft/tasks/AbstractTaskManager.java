package org.workcraft.tasks;

import java.util.ArrayList;

public abstract class AbstractTaskManager implements TaskManager {

    public final TaskObserverList taskObserverList = new TaskObserverList();

    public static class TaskObserverList extends ArrayList<TaskMonitor> implements TaskMonitor {
        @Override
        public <T> ProgressMonitorArray<T> taskStarting(String description) {
            ProgressMonitorArray<T> l = new ProgressMonitorArray<>();
            for (TaskMonitor obs : this) {
                l.add(obs.taskStarting(description));
            }
            return l;
        }
    }

    @Override
    public final void addObserver(TaskMonitor observer) {
        taskObserverList.add(observer);
    }

    @Override
    public final void removeObserver(TaskMonitor observer) {
        taskObserverList.remove(observer);
    }

    @Override
    public final <T> Result<? extends T> execute(Task<T> task, String description) {
        return rawExecute(task, description, null);
    }

    @Override
    public final <T> Result<? extends T> execute(Task<T> task, String description, ProgressMonitor<? super T> observer) {
        return rawExecute(task, description, observer);
    }

    @Override
    public final <T> void queue(final Task<T> task, final String description) {
        queue(task, description, null);
    }

    @Override
    public final <T> void queue(final Task<T> task, final String description, final ProgressMonitor<? super T> observer) {
        Thread thread = new Thread(() -> rawExecute(task, description, observer));
        thread.start();
    }

    public abstract <T> Result<? extends T> rawExecute(Task<T> task, String description, ProgressMonitor<? super T> observer);

}
