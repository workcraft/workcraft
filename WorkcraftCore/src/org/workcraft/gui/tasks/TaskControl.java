package org.workcraft.gui.tasks;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

@SuppressWarnings("serial")
public class TaskControl extends JPanel {
    JLabel label;
    JProgressBar progressBar;
    JButton btnCancel;

    volatile boolean cancelRequested;

    public TaskControl(String taskDescription) {
        setLayout(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL, 80, 120},
                new double[]{25, 25, 25}));

        Border outsideBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        Border insideBorder = SizeHelper.getEmptyBorder();
        Border lineBorder = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
        setBorder(lineBorder);

        label = new JLabel(taskDescription);
        label.setMinimumSize(new Dimension(100, 20));
        label.setPreferredSize(new Dimension(300, 20));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(1000);

        progressBar.setMinimumSize(new Dimension(100, 20));
        progressBar.setPreferredSize(new Dimension(300, 20));

        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(event -> cancel());

        add(label, new TableLayoutConstraints(0, 0, 2, 0));
        add(progressBar, new TableLayoutConstraints(0, 1, 2, 1));
        add(btnCancel, new TableLayoutConstraints(2, 2));
    }

    public void progressUpdate(final double completion) {
        progressBar.setIndeterminate(false);
        progressBar.setValue((int) (completion * 1000));
    }

    public boolean isCancelRequested() {
        return cancelRequested;
    }

    public void cancel() {
        cancelRequested = true;
        btnCancel.setEnabled(false);
        btnCancel.setText("Cancelling...");
    }
}
