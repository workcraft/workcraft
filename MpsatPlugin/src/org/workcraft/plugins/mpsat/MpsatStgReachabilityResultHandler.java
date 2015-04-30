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

	private String getMessage(boolean isSatisfiable, boolean inversePredicate) {
		String propertyName = "Property";
		MpsatSettings settings = task.getSettings();
		if ((settings != null) && (settings.getName() != null)) {
			propertyName = settings.getName();
		}
		String predicateText = "Predicate is " + (isSatisfiable ? "satisfiable" : "unsatisfiable") + ". ";
		String propertyText =  propertyName + (isSatisfiable == inversePredicate ? " is violated." : " holds.");
		return predicateText + propertyText;
	}

	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue().getMpsatResult().getReturnValue());
		List<Solution> solutions = mdp.getSolutions();
		String title = "Verification results";
		String message = getMessage(!solutions.isEmpty(), true);
		if (Solution.hasTraces(solutions)) {
			String extendedMessage = "<html>" + message +  "<br>Trace(s) leading to the problematic states:</html>";
			final SolutionsDialog solutionsDialog = new SolutionsDialog(task, title, extendedMessage, solutions);
			GUI.centerAndSizeToParent(solutionsDialog, Framework.getInstance().getMainWindow());
			solutionsDialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
