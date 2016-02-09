package org.workcraft.tasks;

public interface TaskManager {
    void addObserver (TaskMonitor obs);
    void removeObserver (TaskMonitor obs);
    <T> void queue (Task<T> task, String description);
    <T> void queue (Task<T> task, String description, ProgressMonitor<? super T> monitor);
    <T> Result<? extends T> execute (Task<T> task, String description);
    <T> Result<? extends T> execute (Task<T> task, String description, ProgressMonitor<? super T> monitor);
}
