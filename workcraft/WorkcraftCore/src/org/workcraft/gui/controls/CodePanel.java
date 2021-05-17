package org.workcraft.gui.controls;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.builtin.settings.LogCommonSettings;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.HighlightUtils;
import org.workcraft.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class CodePanel extends JPanel {

    private final Font font = new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize());
    private final TextEditor textArea = new TextEditor(font);
    private final JLabel statusLabel = new JLabel();

    private Object highlight = null;

    public CodePanel(int initialLineCount) {
        super(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        initTextArea(initialLineCount);
        initStatusLabel();
    }

    private void initTextArea(int initialLineCount) {
        textArea.setText(TextUtils.repeat("\n", initialLineCount));
        textArea.addPopupMenu();
        textArea.addUndoManager();
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Reset highlighters on key press
                clearHighlight();
                // Ignore non-ASCII characters
                if (e.getKeyChar() > 127) {
                    e.consume();
                }
            }
        });
    }

    private void initStatusLabel() {
        statusLabel.setVisible(false);
        statusLabel.setOpaque(true);
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        clearHighlight();
        textArea.setText(text);
        textArea.setCaretPosition(0);
        textArea.requestFocus();
    }

    public void highlightError(int fromPos, int toPos, String detail) {
        clearHighlight();
        showErrorStatus(detail);
        highlight = HighlightUtils.highlightText(textArea, fromPos, toPos, LogCommonSettings.getErrorBackground());
        textArea.setCaretPosition(fromPos);
        textArea.moveCaretPosition(fromPos);
        textArea.requestFocus();
    }

    private void clearHighlight() {
        if (highlight != null) {
            textArea.getHighlighter().removeHighlight(highlight);
            highlight = null;
        }
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
