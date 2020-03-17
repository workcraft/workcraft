package org.workcraft.tasks;

public interface TaskMonitor {
    <T> ProgressMonitor<T> taskStarting(String description);
}
