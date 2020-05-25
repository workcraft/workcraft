package org.workcraft.gui.controls;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.builtin.settings.LogCommonSettings;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.TextUtils;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class CodePanel extends JPanel {

    private static final String UNDO_KEY = "Undo";
    private static final String REDO_KEY = "Redo";

    private final UndoManager history = new UndoManager();
    private final JTextArea textArea = new JTextArea();
    private final JLabel statusLabel = new JLabel();

    public CodePanel(int initialLineCount) {
        super(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        initTextArea(initialLineCount);
        initStatusLabel();
    }

    private void initTextArea(int initialLineCount) {
        textArea.setMargin(SizeHelper.getTextMargin());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
        textArea.setText(TextUtils.repeat("\n", initialLineCount));
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Reset highlighters on key press
                clearHighlights();
                // Ignore non-ASCII characters
                if (e.getKeyChar() > 127) {
                    e.consume();
                }
            }
        });

        // Add undo to te action map and bind it to the input map
        textArea.getActionMap().put(UNDO_KEY, new AbstractAction(UNDO_KEY) {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (history.canUndo()) {
                    history.undo();
                }
            }
        });
        textArea.getInputMap().put(DesktopApi.getUndoKeyStroke(), UNDO_KEY);

        // Add redo to te action map and bind it to the input map
        textArea.getActionMap().put(REDO_KEY, new AbstractAction(REDO_KEY) {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (history.canRedo()) {
                    history.redo();
                }
            }
        });
        textArea.getInputMap().put(DesktopApi.getRedoKeyStroke(), REDO_KEY);

        // Listen to the undo/redo events
        textArea.getDocument().addUndoableEditListener(event -> history.addEdit(event.getEdit()));
    }

    private void initStatusLabel() {
        statusLabel.setVisible(false);
        statusLabel.setOpaque(true);
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        history.discardAllEdits();
        clearHighlights();
        textArea.setText(text);
        textArea.setCaretPosition(0);
        textArea.requestFocus();
    }

    public void highlightError(int fromPos, int toPos, String detail) {
        showErrorStatus(detail);
        GuiUtils.highlightText(textArea, fromPos, toPos, LogCommonSettings.getErrorBackground());
        textArea.setCaretPosition(fromPos);
        textArea.moveCaretPosition(fromPos);
        textArea.requestFocus();
    }

    private void clearHighlights() {
        textArea.getHighlighter().removeAllHighlights();
        clearStatus();
    }

    public void showInfoStatus(String message) {
        statusLabel.setBackground(LogCommonSettings.getInfoBackground());
        showStatus(message);
    }

    public void showWarningStatus(String message) {
        statusLabel.setBackground(LogCommonSettings.getWarningBackground());
        showStatus(message);
    }

    public void showErrorStatus(String message) {
        statusLabel.setBackground(LogCommonSettings.getErrorBackground());
        showStatus(message);
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void setDetailsFile(File file) {
        if (file != null) {
            statusLabel.setToolTipText(file.getAbsolutePath());
            statusLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            statusLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    DesktopApi.open(file);
                }
            });
        }
    }

    private void clearStatus() {
        statusLabel.setVisible(false);
        statusLabel.setText(null);
        statusLabel.setToolTipText(null);
        for (MouseListener listener : statusLabel.getMouseListeners()) {
            statusLabel.removeMouseListener(listener);
        }
    }

}
