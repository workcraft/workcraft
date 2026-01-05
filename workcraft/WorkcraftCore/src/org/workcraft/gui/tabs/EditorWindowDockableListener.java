package org.workcraft.gui.tabs;

import org.workcraft.Framework;
import org.workcraft.gui.editor.GraphEditorPanel;

import javax.swing.*;

public class EditorWindowDockableListener implements DockableListener {

    private final GraphEditorPanel editor;

    public EditorWindowDockableListener(GraphEditorPanel editor) {
        this.editor = editor;
    }

    @Override
    public void tabSelected(JTabbedPane tabbedPane, int tabIndex) {
        setNonFocusable(tabbedPane);
        requestEditorFocus();
    }

    @Override
    public void dockedStandalone() {
        requestEditorFocus();
    }

    @Override
    public void dockedInTab(JTabbedPane tabbedPane, int tabIndex) {
        setNonFocusable(tabbedPane);
        requestEditorFocus();
    }

    @Override
    public void headerClicked(int button) {
        requestEditorFocus();
    }

    @Override
    public void windowMaximised() {
        requestEditorFocus();
    }

    @Override
    public void windowRestored() {
        requestEditorFocus();
    }

    private void setNonFocusable(JTabbedPane tabbedPane) {
        // Set non-focusable, so that tab activation does not steal the focus form the included component.
        tabbedPane.setFocusable(false);
    }

    private void requestEditorFocus() {
        Framework.getInstance().getMainWindow().requestFocus(editor);
    }

}
