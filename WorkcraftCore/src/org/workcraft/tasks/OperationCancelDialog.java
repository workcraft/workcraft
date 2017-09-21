package org.workcraft.tasks;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.workcraft.gui.tasks.TaskControl;
import org.workcraft.util.GUI;

final class OperationCancelDialog<T> extends JDialog implements ProgressMonitor<T> {

    private static final long serialVersionUID = 4633136071864781499L;
    private final TaskControl taskControl;
    private Result<? extends T> result;

    OperationCancelDialog(Window parent, String description) {
        setModal(true);
        taskControl = new TaskControl(description);
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout(2, 2));
        content.add(taskControl, BorderLayout.CENTER);
        pack();
        if (parent != null) {
            GUI.centerToParent(this, parent);
        }
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
    public void progressUpdate(double completion) {
        taskControl.progressUpdate(completion);
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
    public void finished(Result<? extends T> result, String description) {
        this.result = result;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(false);
            }
        });
    }

    public Result<? extends T> getResult() {
        return result;
    }

}
