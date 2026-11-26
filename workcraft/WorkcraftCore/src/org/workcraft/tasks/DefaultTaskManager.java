package org.workcraft.tasks;

public class DefaultTaskManager extends AbstractTaskManager {

    @Override
    public <T> Result<? extends T> rawExecute(Task<T> task, String description, ProgressMonitor<? super T> observer) {
        ProgressMonitorArray<T> progressMon = taskObserverList.taskStarting(description);
        if (observer != null) {
            progressMon.add(observer);
        }
        Result<? extends T> result;
        try {
            result = task.run(progressMon);
        } catch (RuntimeException e) {
            // Monitors must always finish, otherwise waiters and the cancel dialog hang;
            // most monitors ignore the cause, so print it here to keep the trace
            e.printStackTrace();
            result = Result.exception(e);
        }
        progressMon.isFinished(result);
        return result;
    }

}
