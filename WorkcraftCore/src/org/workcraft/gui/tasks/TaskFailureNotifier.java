package org.workcraft.gui.tasks;

import javax.swing.SwingUtilities;

import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;

public class TaskFailureNotifier extends DummyProgressMonitor<Object> {
    String errorMessage = "";

    @Override
    public void finished(final Result<? extends Object> result, final String description) {
        if (result.getOutcome() == Outcome.FAILED) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String message = "Task '" + description + "' has failed to complete as expected.";

                    if (result.getCause() != null) {
                        Throwable reason = result.getCause();
                        reason.printStackTrace();
                        message += "\n\nThe reason was: " + reason.toString();
                        message += "\nPlease see the 'Problems' tab for further details.";
                    }

                    if (!errorMessage.isEmpty()) {
                        message += "\n\nFollowing errors were reported:\n" + errorMessage;
                    }

                    DialogUtils.showError(message);
                }
            });
        }
    }

    @Override
    public void stderr(byte[] data) {
        errorMessage = errorMessage + new String(data);
    }
}
