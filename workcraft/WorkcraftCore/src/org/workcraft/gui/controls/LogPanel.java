package org.workcraft.gui.controls;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class LogPanel extends JPanel {

    private final TextEditor textArea = new TextEditor();

    public LogPanel() {
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);

        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(textArea);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        textArea.addPopupMenu();
    }

    public JTextArea getTextArea() {
        return textArea;
    }

}
