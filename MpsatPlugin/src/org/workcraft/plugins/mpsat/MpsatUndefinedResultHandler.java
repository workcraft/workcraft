package org.workcraft.plugins.mpsat;

import javax.swing.JOptionPane;

import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.Result;


final class MpsatUndefinedResultHandler implements Runnable {

	private final Result<? extends MpsatChainResult> result;
	private final MpsatChainTask task;

	MpsatUndefinedResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> result) {
		this.task = task;
		this.result = result;
	}

	@Override
	public void run() {
		String message = result.getReturnValue().getMessage();
		if (message == null) {
			MpsatSettings settings = task.getSettings();
			if (settings != null && settings.getName() != null) {
				message = settings.getName();
			}
		}
		JOptionPane.showMessageDialog(null, message);
	}

}
