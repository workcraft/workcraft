package org.workcraft.gui.tasks;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.ExceptionResult;
import org.workcraft.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.ExitStatus;

public class TaskFailureNotifier extends DummyProgressMonitor {
	String errorMessage = "";


	@Override
	public void finished(final Result result, final String description) {
		if (result.getExitStatus() == ExitStatus.FAILED) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String message = "Unfortunately, the task \"" + description + "\" has failed to complete as expected.";
					if (result instanceof ExceptionResult) {
						Throwable reason = ((ExceptionResult)result).getReason();
						reason.printStackTrace();
						message += "\n\nThe reason was: " + reason.toString();
						message += "\nPlease see the \"Problems\" tab for further details.";
					} else if (result instanceof ExternalProcessResult){
						ExternalProcessResult epr = ((ExternalProcessResult)result);
						message += "\n\nExternal process has returned code " + epr.getReturnCode();
						if (!errorMessage.isEmpty())
							message += " and reported the following errors:\n" + errorMessage;
					}
					JOptionPane.showMessageDialog(null, message, "Task failed", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}

	@Override
	public void logErrorMessage(String message) {
		errorMessage = errorMessage + message;
	}
}
