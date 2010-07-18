package org.workcraft.tasks;

import java.util.ArrayList;

public class ProgressMonitorArray<T> extends ArrayList<ProgressMonitor<? super T>> implements ProgressMonitor <T>
{
	private static final long serialVersionUID = 1L;

	@Override
	public void finished(Result<? extends T> result, String description) {
		for(ProgressMonitor<? super T> o : this)
			o.finished(result, description);
	}

	@Override
	public boolean isCancelRequested() {
		boolean requested = false;
		for(ProgressMonitor<? super T> o : this)
			requested |= o.isCancelRequested();
		return requested;
	}

	@Override
	public void stdout(byte[] data) {
		for(ProgressMonitor<? super T> o : this)
			o.stdout(data);
	}

	@Override
	public void progressUpdate(double completion) {
		for(ProgressMonitor<? super T> o : this)
			o.progressUpdate(completion);
	}

	@Override
	public void stderr(byte[] data) {
		for(ProgressMonitor<? super T> o : this)
			o.stderr(data);
	}
}
