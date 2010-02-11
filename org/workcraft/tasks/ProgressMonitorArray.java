package org.workcraft.tasks;

import java.util.ArrayList;

public class ProgressMonitorArray extends ArrayList<ProgressMonitor> implements ProgressMonitor
{
	private static final long serialVersionUID = 1L;

	@Override
	public void finished(Result result, String description) {
		for(ProgressMonitor o : this)
			o.finished(result, description);
	}

	@Override
	public boolean isCancelRequested() {
		boolean requested = false;
		for(ProgressMonitor o : this)
			requested |= o.isCancelRequested();
		return requested;
	}

	@Override
	public void logMessage(String message) {
		for(ProgressMonitor o : this)
			o.logMessage(message);
	}

	@Override
	public void progressUpdate(double completion) {
		for(ProgressMonitor o : this)
			o.progressUpdate(completion);
	}

	@Override
	public void logErrorMessage(String message) {
		for(ProgressMonitor o : this)
			o.logErrorMessage(message);
	}
}
