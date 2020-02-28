package org.workcraft.gui.controls;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.DesktopApi;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;

public class FlatTextArea extends JTextArea {

    private static final String UNDO_KEY = "Undo";
    private static final String REDO_KEY = "Redo";
    private final UndoManager history = new UndoManager();

    public FlatTextArea() {
        super();
        setBorder(SizeHelper.getGapBorder());

        // Add undo to te action map and bind it to the input map
        getActionMap().put(UNDO_KEY, new AbstractAction(UNDO_KEY) {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (history.canUndo()) {
                    history.undo();
                }
            }
        });
        getInputMap().put(DesktopApi.getUndoKeyStroke(), UNDO_KEY);

        // Add redo to te action map and bind it to the input map
        getActionMap().put(REDO_KEY, new AbstractAction(REDO_KEY) {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (history.canRedo()) {
                    history.redo();
                }
            }
        });
        getInputMap().put(DesktopApi.getRedoKeyStroke(), REDO_KEY);

        // Listen to the undo/redo events
        getDocument().addUndoableEditListener(event -> history.addEdit(event.getEdit()));
    }

    public void discardEditHistory() {
        history.discardAllEdits();
    }

}
