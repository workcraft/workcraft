/**
 *
 */
package org.workcraft.plugins.verification;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.plugins.verification.tasks.MpsatChainResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;

public class MpsatChainResultHandler extends DummyProgressMonitor<MpsatChainResult> {
	@Override
	public void finished(final Result<? extends MpsatChainResult> mpsatChainResult, String description) {
		final MpsatMode mpsatMode = mpsatChainResult.getReturnValue().getMpsatMode();
		switch (mpsatMode) {
		case DEADLOCK:
			SwingUtilities.invokeLater(new MpsatDeadlockResultHandler(mpsatChainResult));
			break;
		default:
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, "MPSat mode \"" + mpsatMode.getArgument() + "\" not (yet) supported." , ":-(", JOptionPane.WARNING_MESSAGE);
				}
			});
			break;
		}
	}
}