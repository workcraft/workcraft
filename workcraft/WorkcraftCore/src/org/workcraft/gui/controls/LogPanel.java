package org.workcraft.gui.controls;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.DesktopApi;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.KeyEvent;

public class LogPanel extends JPanel {

    private final JTextArea textArea;

    public LogPanel() {
        textArea = new JTextArea();
        textArea.setMargin(SizeHelper.getTextMargin());
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);

        JPopupMenu popup = new JPopupMenu();

        JMenuItem miCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
        miCopy.setText("Copy");
        miCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, DesktopApi.getMenuKeyMask()));
        miCopy.setMnemonic(KeyEvent.VK_C);
        popup.add(miCopy);

        JMenuItem miSelect = new JMenuItem("Select all");
        miSelect.addActionListener(event -> textArea.selectAll());
        miSelect.setText("Select All");
        miSelect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, DesktopApi.getMenuKeyMask()));
        miSelect.setMnemonic(KeyEvent.VK_A);
        popup.add(miSelect);

        JMenuItem miClear = new JMenuItem("Clear");
        miClear.addActionListener(event -> textArea.setText(""));
        miClear.setMnemonic(KeyEvent.VK_R);
        popup.add(miClear);

        textArea.setComponentPopupMenu(popup);

        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(textArea);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    public JTextArea getTextArea() {
        return textArea;
    }

}
