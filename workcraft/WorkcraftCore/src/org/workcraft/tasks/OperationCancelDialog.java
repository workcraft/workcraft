package org.workcraft.tasks;

import org.workcraft.gui.tasks.TaskControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

final class OperationCancelDialog<T> extends JDialog implements ProgressMonitor<T> {

    private static final long serialVersionUID = 4633136071864781499L;
    private final TaskControl taskControl;
    private boolean finished = false;
    private Result<? extends T> result = null;

    OperationCancelDialog(Window owner, String description) {
        setModal(true);
        taskControl = new TaskControl(description);
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout(2, 2));
        content.add(taskControl, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                taskControl.cancel();
            }
        });
        doLayout();
    }


    @Override
    public void setDetails(String details) {
        taskControl.setDetails(details);
    }

    @Override
    public void progressUpdate(double completion) {
        taskControl.setProgress(completion);
    }

    @Override
    public void stdout(byte[] data) {
    }

    @Override
    public void stderr(byte[] data) {
    }

    @Override
    public boolean isCancelRequested() {
        return taskControl.isCancelRequested();
    }

    @Override
    public void isFinished(Result<? extends T> result) {
        this.result = result;
        SwingUtilities.invokeLater(() -> setVisible(false));
        finished = true;
    }

    @Override
    public Result<? extends T> waitResult() {
        while (!finished) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return null;
            }
        }
        return result;
    }

}
