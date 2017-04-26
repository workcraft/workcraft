package org.workcraft.gui;

import javax.swing.JTabbedPane;

import org.workcraft.Framework;
import org.workcraft.gui.graph.GraphEditorPanel;

public class EditorWindowTabListener implements DockableWindowTabListener {

    private final GraphEditorPanel editor;

    public EditorWindowTabListener(GraphEditorPanel editor) {
        this.editor = editor;
    }

    @Override
    public void tabSelected(JTabbedPane tabbedPane, int tabIndex) {
        setNonFocusable(tabbedPane);
        requestEditorFocus();
    }

    @Override
    public void tabDeselected(JTabbedPane tabbedPane, int tabIndex) {
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
    public void headerClicked() {
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
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        mainWindow.requestFocus(editor);
    }

}
