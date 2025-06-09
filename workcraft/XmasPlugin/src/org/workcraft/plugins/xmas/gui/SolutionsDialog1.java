package org.workcraft.plugins.xmas.gui;

import javax.swing.*;
import java.awt.*;

public class SolutionsDialog1 extends JDialog {

    public SolutionsDialog1(int n, String str) {
        JPanel contents = new JPanel();
        contents.setLayout(new BorderLayout());

        if (n == 1) {
            JLabel label = new JLabel("The system has a deadlock");
            contents.add(label, BorderLayout.NORTH);
        } else {
            JLabel label = new JLabel("Local deadlocks found");
            contents.add(label, BorderLayout.NORTH);
        }

        JPanel solutionsPanel = new JPanel();
        solutionsPanel.setLayout(new BoxLayout(solutionsPanel, BoxLayout.Y_AXIS));

        solutionsPanel.add(new SolutionPanel(str));

        contents.add(solutionsPanel, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(event -> setVisible(false));

        buttonsPanel.add(okButton);
        contents.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(contents);
        pack();
        setVisible(true);
    }

}
