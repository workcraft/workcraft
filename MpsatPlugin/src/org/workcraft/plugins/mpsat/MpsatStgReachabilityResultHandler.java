package org.workcraft.plugins.mpsat;

import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Trace;
import org.workcraft.plugins.mpsat.gui.SolutionsDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;


final class MpsatStgReachabilityResultHandler implements Runnable {

	private final Result<? extends MpsatChainResult> result;
	private final MpsatChainTask task;

	MpsatStgReachabilityResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> result) {
		this.task = task;
		this.result = result;
	}


	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue().getMpsatResult().getReturnValue());
		List<Trace> solutions = mdp.getSolutions();
		if (!solutions.isEmpty()) {
			String message = "The system has a non-persistent output.\n";
			final SolutionsDialog solutionsDialog = new SolutionsDialog(task, message, solutions);
			GUI.centerAndSizeToParent(solutionsDialog, task.getFramework().getMainWindow());
			solutionsDialog.setVisible(true);
		} else {
			String message = result.getReturnValue().getMessage();
			if (message == null) {
				message = "All system outputs are persistent.";
			}
			JOptionPane.showMessageDialog(null, message);
		}
	}

}
