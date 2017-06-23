package org.workcraft.gui.tasks;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import org.workcraft.dom.visual.SizeHelper;

import info.clearthought.layout.TableLayout;

@SuppressWarnings("serial")
public class TaskControl extends JPanel {
    JLabel label;
    JProgressBar progressBar;
    JButton btnCancel;

    volatile boolean cancelRequested;

    public TaskControl(String taskDescription) {
        double[][] size = {
            {TableLayout.FILL, 80, 100},
            {20, 20, 20},
        };

        Border outsideBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        Border insideBorder = SizeHelper.getEmptyBorder();
        Border lineBorder = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);

        setBorder(lineBorder);

        TableLayout layout = new TableLayout(size);
        layout.setHGap(3);
        layout.setVGap(3);
        setLayout(layout);

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
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        add(label, "0,0,2,0");
        add(progressBar, "0,1,2,1");
        add(btnCancel, "2,2");
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
