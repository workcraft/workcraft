package org.workcraft.plugins.mpsat;

import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.gui.EncodingConflictDialog;
import org.workcraft.plugins.mpsat.gui.Solution;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;


final class MpsatEncodingConflictResultHandler implements Runnable {

	private final Result<? extends MpsatChainResult> result;
	private final MpsatChainTask task;

	MpsatEncodingConflictResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> result) {
		this.task = task;
		this.result = result;
	}

	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue().getMpsatResult().getReturnValue());
		List<Solution> solutions = mdp.getSolutions();
		String title = "Verification results";
		String message = (solutions.isEmpty() ? "No encodning conflicts." : "Encoding conflicts detected.");
		if (Solution.hasTraces(solutions)) {
			String extendedMessage = "<html><br>&#160;" + message +  "<br><br>&#160;Pairs of traces leading to the entrance and exit of the encoding conflict cores(s):<br><br></html>";
			final EncodingConflictDialog dialog = new EncodingConflictDialog(task, title, extendedMessage, solutions);
			GUI.centerToParent(dialog, Framework.getInstance().getMainWindow());
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
