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
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        mainWindow.requestFocus(editor);
    }

    @Override
    public void tabDeselected(JTabbedPane tabbedPane, int tabIndex) {
    }

    @Override
    public void dockedStandalone() {
    }

    @Override
    public void dockedInTab(JTabbedPane tabbedPane, int tabIndex) {
    }

    @Override
    public void headerClicked() {
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        mainWindow.requestFocus(editor);
    }

}
