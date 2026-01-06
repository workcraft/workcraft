package org.workcraft.gui.panels;

import org.workcraft.gui.controls.TextEditor;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class LogPanel extends JPanel {

    private final TextEditor textEditor = new TextEditor();

    public LogPanel(Runnable updater) {
        textEditor.setLineWrap(true);
        textEditor.setEditable(false);
        textEditor.setWrapStyleWord(true);

        DefaultCaret caret = (DefaultCaret) textEditor.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(textEditor);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        textEditor.addPopupMenu();

        if (updater != null) {
            textEditor.addUpdateListener(updater);
        }
    }

    public TextEditor getTextEditor() {
        return textEditor;
    }

    public boolean isEmpty() {
        return textEditor.isEmpty();
    }

}
