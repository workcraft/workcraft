package org.workcraft.tasks;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;

public class ExtendedTaskManager extends DefaultTaskManager {

    @Override
    public <T> Result<? extends T> rawExecute(Task<T> task, String description, ProgressMonitor<? super T> observer) {
        if (!SwingUtilities.isEventDispatchThread()) {
            return super.rawExecute(task, description, observer);
        } else {
            MainWindow mainWindow = Framework.getInstance().getMainWindow();
            OperationCancelDialog<T> cancelDialog = new OperationCancelDialog<>(mainWindow, description);
            ProgressMonitorArray<T> observers = new ProgressMonitorArray<>();
            if (observer != null) {
                observers.add(observer);
            }
            observers.add(cancelDialog);
            this.queue(task, description, observers);
            cancelDialog.setVisible(true);
            return cancelDialog.waitResult();
        }
    }

}
