package org.workcraft.gui.panels;

import org.workcraft.gui.controls.TextEditor;
import org.workcraft.types.Pair;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import java.awt.*;

public class LogPanel extends JPanel {

    private final TextEditor textEditor = new TextEditor();
    private final JScrollPane scrollPane = new JScrollPane();

    private DocumentListener documentChangeListener = null;
    private ChangeListener viewportChangeListener = null;

    public LogPanel() {
        textEditor.setLineWrap(true);
        textEditor.setEditable(false);
        textEditor.setWrapStyleWord(true);

        DefaultCaret caret = (DefaultCaret) textEditor.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        scrollPane.setViewportView(textEditor);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        textEditor.addPopupMenu();
    }

    public void registerContentChangeListener(Runnable contentChangeRunner) {
        Document document = textEditor.getDocument();
        if (documentChangeListener != null) {
            document.removeDocumentListener(documentChangeListener);
        }
        if (contentChangeRunner != null) {
            documentChangeListener = new DocumentListener() {
                @Override
                public void removeUpdate(DocumentEvent e) {
                    contentChangeRunner.run();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    contentChangeRunner.run();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    contentChangeRunner.run();
                }
            };
            document.addDocumentListener(documentChangeListener);
        }
    }

    public void registerViewportChangeListener(Runnable viewportChangeRunner) {
        JViewport viewport = scrollPane.getViewport();
        if (viewportChangeListener != null) {
            viewport.removeChangeListener(viewportChangeListener);
        }
        if (viewportChangeRunner != null) {
            viewportChangeListener = e -> viewportChangeRunner.run();
            viewport.addChangeListener(viewportChangeListener);
        }
    }

    public TextEditor getTextEditor() {
        return textEditor;
    }

    public Pair<Integer, Integer> getVisibleRange() {
        Rectangle viewRect = scrollPane.getViewport().getViewRect();
        Point p = viewRect.getLocation();
        int startPos = textEditor.viewToModel2D(p);

        p.x += viewRect.width;
        p.y += viewRect.height;
        int endPos = textEditor.viewToModel2D(p);
        return Pair.of(startPos, endPos);
    }

    public boolean isEmpty() {
        return textEditor.isEmpty();
    }

    public void clear() {
    }

}
