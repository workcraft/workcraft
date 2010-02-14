package org.workcraft.tasks;

public interface ProgressMonitor<T> {
	public void progressUpdate(double completion);
	public void stdout(byte[] data);
	public void stderr(byte[] data);
	public boolean isCancelRequested();
	public void finished(Result<? extends T> result, String description);
}