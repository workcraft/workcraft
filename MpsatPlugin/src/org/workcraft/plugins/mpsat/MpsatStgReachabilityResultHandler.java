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


final class MpsatStgReachabilityResultHandler implements Runnable {

	private final Result<? extends MpsatChainResult> result;
	private final MpsatChainTask task;

	MpsatStgReachabilityResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> result) {
		this.task = task;
		this.result = result;
	}

	private String getPropertyName() {
		String result;
		MpsatSettings settings = task.getSettings();
		if ((settings != null) && (settings.getName() != null)) {
			result = settings.getName();
		} else {
			result = "The property";
		}
		return result;
	}

	private String getSatisfiableMessage() {
		String result;
		MpsatSettings settings = task.getSettings();
		if ((settings != null) && (settings.getSatisfiableMessage() != null)) {
			result = settings.getSatisfiableMessage();
		} else {
			result = getPropertyName() + " predicate is satisfiable.";
		}
		return result;
	}

	private String getUnsatisfiableMessage() {
		String result;
		MpsatSettings settings = task.getSettings();
		if ((settings != null) && (settings.getUnsatisfiableMessage() != null)) {
			result = settings.getUnsatisfiableMessage();
		} else {
			result = getPropertyName() + " predicate is unsatisfiable.";
		}
		return result;
	}

	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue().getMpsatResult().getReturnValue());
		List<Solution> solutions = mdp.getSolutions();
		String title = "Verification results";
		String message = result.getReturnValue().getMessage();
		if (solutions.isEmpty()) {
			if (message == null) {
				message = getUnsatisfiableMessage();
			}
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
		} else if (!Solution.hasTraces(solutions)) {
			if (message == null) {
				message = getSatisfiableMessage();
			}
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
		} else {
			if (message == null) {
				message = getSatisfiableMessage() + " Trace(s) leading to the problematic state:";
			}
			final SolutionsDialog solutionsDialog = new SolutionsDialog(task, title, message, solutions);
			GUI.centerAndSizeToParent(solutionsDialog, Framework.getInstance().getMainWindow());
			solutionsDialog.setVisible(true);
		}
	}

}
