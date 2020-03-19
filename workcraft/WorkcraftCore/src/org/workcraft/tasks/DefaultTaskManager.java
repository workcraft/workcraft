package org.workcraft.tasks;

public class DefaultTaskManager extends AbstractTaskManager {

    @Override
    public <T> Result<? extends T> rawExecute(Task<T> task, String description, ProgressMonitor<? super T> observer) {
        ProgressMonitorArray<T> progressMon = taskObserverList.taskStarting(description);
        if (observer != null) {
            progressMon.add(observer);
        }
        Result<? extends T> result = task.run(progressMon);
        progressMon.isFinished(result);
        return result;
    }

}
