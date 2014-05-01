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

	private String getPropertyName() {
		String propertyName;
		MpsatSettings settings = task.getSettings();
		if (settings != null && settings.getName() != null) {
			propertyName = settings.getName();
		} else {
			propertyName = "The property";
		}
		return propertyName;
	}

	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue().getMpsatResult().getReturnValue());
		List<Trace> solutions = mdp.getSolutions();
		String message = result.getReturnValue().getMessage();
		if (!solutions.isEmpty()) {
			if (message == null) {
				message = getPropertyName() + " is violated with the following trace:\n";
			}
			final SolutionsDialog solutionsDialog = new SolutionsDialog(task, message, solutions);
			GUI.centerAndSizeToParent(solutionsDialog, task.getFramework().getMainWindow());
			solutionsDialog.setVisible(true);
		} else {
			if (message == null) {
				message = getPropertyName() + " is satisfied.";
			}
			JOptionPane.showMessageDialog(null, message);
		}
	}

}
