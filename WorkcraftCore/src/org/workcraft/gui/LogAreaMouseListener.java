package org.workcraft.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

public final class LogAreaMouseListener implements MouseListener {
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object src = e.getSource();
        if (src instanceof JTextArea) {
            final JTextArea textArea = (JTextArea) src;
            if (e.getButton() == MouseEvent.BUTTON3) {
                JPopupMenu popup = new JPopupMenu();
                popup.setFocusable(false);

                // Copy
                JMenuItem miCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
                miCopy.setText("Copy");
                miCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
                miCopy.setMnemonic(KeyEvent.VK_C);
                popup.add(miCopy);

                // Select all
                JMenuItem miSelect = new JMenuItem("Select all");
                miSelect.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        textArea.selectAll();
                    }
                });
                miSelect.setText("Select All");
                miSelect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
                miCopy.setMnemonic(KeyEvent.VK_A);
                popup.add(miSelect);

                // Clear
                JMenuItem miClear = new JMenuItem("Clear");
                miCopy.setMnemonic(KeyEvent.VK_R);
                miClear.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        textArea.setText("");
                    }
                });
                popup.add(miClear);

                popup.show(textArea, e.getX(), e.getY());
            }
        }
    }
}
