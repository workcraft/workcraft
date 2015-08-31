/**
 *
 */
package org.workcraft.plugins.mpsat;

import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.gui.Solution;
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
		List<Solution> solutions = mdp.getSolutions();
		String title = "Verification results";
		if (solutions.isEmpty()) {
			String message = "The system is deadlock-free.";
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
		} else if (!Solution.hasTraces(solutions)) {
			String message = "The system has a deadlock.";
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
		} else {
			String message = "<html><br>&#160;The system has a deadlock after the following trace(s):<br><br></html>";
			final SolutionsDialog solutionsDialog = new SolutionsDialog(task, title, message, solutions);
			GUI.centerToParent(solutionsDialog, Framework.getInstance().getMainWindow());
			solutionsDialog.setVisible(true);
		}
	}
}
