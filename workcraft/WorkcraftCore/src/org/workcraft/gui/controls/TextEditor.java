package org.workcraft.gui.controls;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.DesktopApi;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class TextEditor extends JTextArea {

    private final float defaultFontSize;

    private UndoManager history;

    public TextEditor() {
        this(UIManager.getFont("TextArea.font"));
    }

    public TextEditor(Font font) {
        setFont(font);
        defaultFontSize = font.getSize2D();
        setMargin(SizeHelper.getTextMargin());

        addKeystrokeAction(DesktopApi.getIncreaseKeyStroke(), () -> {
            float size = getFont().getSize2D() + defaultFontSize / 10.0f;
            setFont(getFont().deriveFont(size));
        });

        addKeystrokeAction(DesktopApi.getDecreaseKeyStroke(), () -> {
            float size = getFont().getSize2D() - defaultFontSize / 10.0f;
            setFont(getFont().deriveFont(size));
        });

        addKeystrokeAction(DesktopApi.getRestoreKeyStroke(), () -> setFont(getFont().deriveFont(defaultFontSize)));
    }

    public void addPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
        miCopy.setText("Copy");
        miCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, DesktopApi.getMenuKeyMask()));
        miCopy.setMnemonic(KeyEvent.VK_C);
        popup.add(miCopy);

        if (isEditable()) {
            JMenuItem miPaste = new JMenuItem(new DefaultEditorKit.PasteAction());
            miPaste.setText("Paste");
            miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, DesktopApi.getMenuKeyMask()));
            miPaste.setMnemonic(KeyEvent.VK_P);
            popup.add(miPaste);

            JMenuItem miCut = new JMenuItem(new DefaultEditorKit.CutAction());
            miCut.setText("Cut");
            miCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, DesktopApi.getMenuKeyMask()));
            miCut.setMnemonic(KeyEvent.VK_T);
            popup.add(miCut);

            popup.addSeparator();
        }

        JMenuItem miSelect = new JMenuItem("Select all");
        miSelect.addActionListener(event -> selectAll());
        miSelect.setText("Select All");
        miSelect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, DesktopApi.getMenuKeyMask()));
        miSelect.setMnemonic(KeyEvent.VK_A);
        popup.add(miSelect);

        JMenuItem miClear = new JMenuItem("Clear");
        miClear.addActionListener(event -> clear());
        miClear.setMnemonic(KeyEvent.VK_R);
        popup.add(miClear);

        setComponentPopupMenu(popup);
    }

    public void addKeystrokeAction(KeyStroke keystroke, Runnable action) {
        // Bind keystroke it to the input map and add it to the action map
        getInputMap().put(keystroke, keystroke);
        getActionMap().put(keystroke, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    public void addUndoManager() {
        // Listen to the undo/redo events
        history = new UndoManager();
        getDocument().addUndoableEditListener(event -> history.addEdit(event.getEdit()));

        addKeystrokeAction(DesktopApi.getUndoKeyStroke(), () -> {
            if (history.canUndo()) {
                history.undo();
            }
        });

        addKeystrokeAction(DesktopApi.getRedoKeyStroke(), () -> {
            if (history.canRedo()) {
                history.redo();
            }
        });
    }

    public boolean isEmpty() {
        return getDocument().getLength() <= 0;
    }

    @Override
    public void setText(String text) {
        if (history != null) {
            history.discardAllEdits();
        }
        super.setText(text);
    }

    public void clear() {
        setText("");
    }

}
