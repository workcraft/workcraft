package org.workcraft.gui.tasks;

import org.workcraft.tasks.BasicProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.utils.DialogUtils;

import javax.swing.*;

public class TaskFailureNotifier extends BasicProgressMonitor<Object> {
    private final String description;
    private String errorMessage = "";

    public TaskFailureNotifier(String description) {
        this.description = description;
    }

    @Override
    public void stderr(byte[] data) {
        errorMessage += new String(data);
    }

    @Override
    public void isFinished(final Result<? extends Object> result) {
        super.isFinished(result);
        if (result.isFailure()) {
            SwingUtilities.invokeLater(() -> {
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
            });
        }
    }

}
