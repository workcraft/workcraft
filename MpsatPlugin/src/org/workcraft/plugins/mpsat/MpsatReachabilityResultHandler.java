package org.workcraft.plugins.mpsat;

import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.gui.Solution;
import org.workcraft.plugins.mpsat.gui.ReachibilityDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;


final class MpsatReachabilityResultHandler implements Runnable {

	private final Result<? extends MpsatChainResult> result;
	private final MpsatChainTask task;

	MpsatReachabilityResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> result) {
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
		String propertyStatus = isSatisfiable == inversePredicate ? " is violated." : " holds.";
		return (propertyName + propertyStatus);
	}

	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue().getMpsatResult().getReturnValue());
		List<Solution> solutions = mdp.getSolutions();
		String title = "Verification results";
		String message = getMessage(!solutions.isEmpty());
		if (Solution.hasTraces(solutions)) {
			String extendedMessage = "<html><br>&#160;" + message +  "<br><br>&#160;Trace(s) leading to the problematic state(s):<br><br></html>";
			final ReachibilityDialog dialog = new ReachibilityDialog(task, title, extendedMessage, solutions);
			GUI.centerToParent(dialog, Framework.getInstance().getMainWindow());
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
