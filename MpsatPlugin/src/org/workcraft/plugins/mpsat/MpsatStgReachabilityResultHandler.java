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

	private String getMessage(boolean isSatisfiable) {
		String propertyName = "Property";
		MpsatSettings settings = task.getSettings();
		if ((settings == null) && (result.getReturnValue() != null)) {
			 settings = result.getReturnValue().getMpsatSettings();
		}
		if ((settings != null) && (settings.getName() != null) && !settings.getName().isEmpty()) {
			propertyName = settings.getName();
		}
		boolean inversePredicate = true;
		if (settings != null) {
			inversePredicate = settings.getInversePredicate();
		}
		String propertyText =  propertyName + (isSatisfiable == inversePredicate ? " is violated" : " holds ");
		String predicateText = " (its predicate is " + (isSatisfiable ? "satisfiable" : "unsatisfiable") + ").";
		return propertyText + predicateText;
	}

	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue().getMpsatResult().getReturnValue());
		List<Solution> solutions = mdp.getSolutions();
		String title = "Verification results";
		String message = getMessage(!solutions.isEmpty());
		if (Solution.hasTraces(solutions)) {
			String extendedMessage = "<html><br>&#160;" + message +  "<br><br>&#160;Trace(s) leading to the problematic state(s):<br><br></html>";
			final SolutionsDialog solutionsDialog = new SolutionsDialog(task, title, extendedMessage, solutions);
			GUI.centerToParent(solutionsDialog, Framework.getInstance().getMainWindow());
			solutionsDialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
