package org.workcraft.tasks;

public interface Task <T> {
    Result<? extends T> run (ProgressMonitor<? super T> monitor);
}
