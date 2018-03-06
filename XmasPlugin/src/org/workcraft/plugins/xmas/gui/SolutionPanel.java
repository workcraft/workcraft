package org.workcraft.plugins.xmas.gui;

import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.workcraft.dom.visual.SizeHelper;

@SuppressWarnings("serial")
public class SolutionPanel extends JPanel {

    public SolutionPanel(final String str, final ActionListener closeAction) {
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
