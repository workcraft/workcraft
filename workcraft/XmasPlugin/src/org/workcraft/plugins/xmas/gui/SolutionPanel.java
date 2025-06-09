package org.workcraft.plugins.xmas.gui;

import org.workcraft.dom.visual.SizeHelper;

import javax.swing.*;

public class SolutionPanel extends JPanel {

    public SolutionPanel(final String str) {
        JTextArea sText = new JTextArea();
        sText.setMargin(SizeHelper.getTextMargin());
        sText.setColumns(50);
        sText.setLineWrap(true);
        sText.setText(str);

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(sText);
        add(scrollPane);
    }

}
