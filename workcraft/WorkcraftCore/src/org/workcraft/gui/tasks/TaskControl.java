package org.workcraft.gui.tasks;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class TaskControl extends JPanel {

    private final JLabel detailsLabel;
    private final JProgressBar progressBar;
    private final JButton cancelButton;

    private volatile boolean cancelRequested;

    public TaskControl(String taskDescription) {
        setLayout(GuiUtils.createBorderLayout());

        Border outsideBorder = new LineBorder(Color.LIGHT_GRAY);
        Border insideBorder = GuiUtils.getEmptyBorder();
        Border lineBorder = new CompoundBorder(outsideBorder, insideBorder);
        setBorder(lineBorder);

        JLabel descriptionLabel = new JLabel(taskDescription);
        detailsLabel = new JLabel();

        JPanel labelPanel = new JPanel(GuiUtils.createFlowLayout());
        labelPanel.add(descriptionLabel);
        labelPanel.add(detailsLabel);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(1000);
        progressBar.setMinimumSize(new Dimension(100, 20));
        progressBar.setPreferredSize(new Dimension(300, 20));

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> cancel());

        JPanel buttonsPanel = GuiUtils.createDialogButtonsPanel();
        buttonsPanel.add(cancelButton);

        add(labelPanel, BorderLayout.NORTH);
        add(progressBar, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    public void setProgress(final double completion) {
        progressBar.setIndeterminate(false);
        progressBar.setValue((int) (completion * 1000));
    }

    public void setDetails(String details) {
        detailsLabel.setText(details);
    }

    public boolean isCancelRequested() {
        return cancelRequested;
    }

    public void cancel() {
        cancelRequested = true;
        cancelButton.setEnabled(false);
        cancelButton.setText("Cancelling...");
    }

}
