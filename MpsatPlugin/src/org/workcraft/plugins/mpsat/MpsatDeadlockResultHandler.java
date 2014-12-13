/**
 *
 */
package org.workcraft.plugins.mpsat;

import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.plugins.mpsat.gui.SolutionsDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;

final class MpsatDeadlockResultHandler implements Runnable {
	private final Result<? extends MpsatChainResult> result;
	private final MpsatChainTask task;

	MpsatDeadlockResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> result) {
		this.task = task;
		this.result = result;
	}

	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue().getMpsatResult().getReturnValue());

		List<Trace> solutions = mdp.getSolutions();

		if (!solutions.isEmpty()) {
			String message = "The system has a deadlock.\n";
			final SolutionsDialog solutionsDialog = new SolutionsDialog(task, message, solutions);
			final Framework framework = Framework.getInstance();
			GUI.centerAndSizeToParent(solutionsDialog, framework.getMainWindow());
			solutionsDialog.setVisible(true);
		} else {
			String message = result.getReturnValue().getMessage();
			if (message == null) {
				message = "The system is deadlock-free.";
			}
			JOptionPane.showMessageDialog(null, message);
		}
	}
}
