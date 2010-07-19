/**
 *
 */
package org.workcraft.plugins.shared;

import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.plugins.shared.tasks.MpsatChainResult;
import org.workcraft.plugins.verification.gui.SolutionsDialog;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;

final class MpsatDeadlockResultHandler implements Runnable {
	private final Result<? extends MpsatChainResult> mpsatChainResult;
	private final Framework framework;

	MpsatDeadlockResultHandler(
			Framework framework,
			Result<? extends MpsatChainResult> mpsatChainResult) {
		this.framework = framework;
		this.mpsatChainResult = mpsatChainResult;
	}

	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue());

		List<Trace> solutions = mdp.getSolutions();

		if (!solutions.isEmpty()) {
			String message = "The system has a deadlock.\n";

			final SolutionsDialog solutionsDialog = new SolutionsDialog(message, solutions);

			GUI.centerAndSizeToParent(solutionsDialog, framework.getMainWindow());

			solutionsDialog.setVisible(true);
		} else
			JOptionPane.showMessageDialog(null, "The system is deadlock-free.");
	}
}