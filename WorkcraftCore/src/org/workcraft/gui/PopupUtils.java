package org.workcraft.gui;

import org.workcraft.utils.DesktopApi;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.event.KeyEvent;

public class PopupUtils {

    public static void setTextAreaPopup(JTextArea textArea) {
        setTextAreaPopup(textArea, true, true, true);
    }

    public static void setTextAreaPopup(JTextArea textArea, boolean hasCopy, boolean hasSelect, boolean hasClear) {
        JPopupMenu popup = new JPopupMenu();

        if (hasCopy) {
            JMenuItem miCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
            miCopy.setText("Copy");
            miCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, DesktopApi.getMenuKeyMask()));
            miCopy.setMnemonic(KeyEvent.VK_C);
            popup.add(miCopy);
        }

        if (hasSelect) {
            JMenuItem miSelect = new JMenuItem("Select all");
            miSelect.addActionListener(event -> textArea.selectAll());
            miSelect.setText("Select All");
            miSelect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, DesktopApi.getMenuKeyMask()));
            miSelect.setMnemonic(KeyEvent.VK_A);
            popup.add(miSelect);
        }

        if (hasClear) {
            JMenuItem miClear = new JMenuItem("Clear");
            miClear.addActionListener(event -> textArea.setText(""));
            miClear.setMnemonic(KeyEvent.VK_R);
            popup.add(miClear);
        }

        textArea.setComponentPopupMenu(popup);
    }

}
