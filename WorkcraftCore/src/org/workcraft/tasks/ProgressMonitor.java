package org.workcraft.tasks;

public interface ProgressMonitor<T> {
    void progressUpdate(double completion);
    void stdout(byte[] data);
    void stderr(byte[] data);
    boolean isCancelRequested();
    void finished(Result<? extends T> result, String description);
}
