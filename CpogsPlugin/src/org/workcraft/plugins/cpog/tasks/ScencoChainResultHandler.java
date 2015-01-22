package org.workcraft.plugins.cpog.tasks;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;

public class ScencoChainResultHandler extends DummyProgressMonitor<ScencoChainResult> {
	private String errorMessage;
	private final ScencoChainTask task;

	public ScencoChainResultHandler(ScencoChainTask task) {
		this.task = task;
	}

	@Override
	public void finished(final Result<? extends ScencoChainResult> result, String description) {
		if (result.getOutcome() == Outcome.FINISHED) {
			final GenerationMode scencoMode = result.getReturnValue().getScencoSettings().getGenMode();
			switch (scencoMode) {
			case OPTIMAL_ENCODING:
			case RECURSIVE:
				//SwingUtilities.invokeLater(new MpsatStgReachabilityResultHandler(task, result));
				break;
			default:
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null,
								"Scenco mode \"" + scencoMode + "\" is not (yet) supported." ,
								"Sorry..", JOptionPane.WARNING_MESSAGE);
					}
				});
				break;
			}
		}
		else if (result.getOutcome() != Outcome.CANCELLED) {
			errorMessage = "Scenco tool chain execution failed :-(";

			Throwable cause1 = result.getCause();

			if (cause1 != null) {
				// Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
				errorMessage += "\n\nFailure caused by: " + cause1.toString() + "\nPlease see the \"Problems\" tab for more details.";
			} else
			{
				Result<? extends Object> exportResult = result.getReturnValue().getExportResult();
				if (exportResult.getOutcome() == Outcome.FAILED) {
					errorMessage += "\n\nFailed to export the model as a .g file.";
					Throwable cause = exportResult.getCause();
					if (cause != null)
						errorMessage += "\n\nFailure caused by: " + cause.toString() + "\nPlease see the \"Problems\" tab for more details.";
					else
						errorMessage += "\n\nThe exporter class did not offer further explanation.";
				} else {
					Result<? extends ExternalProcessResult> encoderResult = result.getReturnValue().getEncoderResult();
					if (encoderResult.getOutcome() == Outcome.FAILED) {
						errorMessage += "\n\nEncoder failed to execute as expected.";
						Throwable cause = encoderResult.getCause();
						if (cause != null)
							errorMessage += "\n\nFailure caused by: " + cause.toString() + "\nPlease see the \"Problems\" tab for more details.";
						else
							errorMessage += "\n\nFailure caused by the following errors:\n" + new String(encoderResult.getReturnValue().getErrors());
					}
					else {
						errorMessage += "\n\nEncoder chain task returned failure status without further explanation. This should not have happened -_-a.";
					}
				}
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, errorMessage, "Oops..", JOptionPane.ERROR_MESSAGE);				}
			});
		}
	}
}