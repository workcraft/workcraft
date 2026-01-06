package org.workcraft.gui.tabs;

import org.workcraft.Framework;
import org.workcraft.gui.editor.GraphEditorPanel;

import javax.swing.*;

public class EditorPanelDockable extends PanelDockable {

    private record FocusDockableListener(GraphEditorPanel editorPanel) implements DockableListener {

        @Override
        public void tabSelected(JTabbedPane tabbedPane, int tabIndex) {
            setNonFocusable(tabbedPane);
            requestEditorPanelFocus();
        }

        @Override
        public void dockedStandalone() {
            requestEditorPanelFocus();
        }

        @Override
        public void dockedInTab(JTabbedPane tabbedPane, int tabIndex) {
            setNonFocusable(tabbedPane);
            requestEditorPanelFocus();
        }

        @Override
        public void headerClicked(int button) {
            requestEditorPanelFocus();
        }

        @Override
        public void windowMaximised() {
            requestEditorPanelFocus();
        }

        @Override
        public void windowRestored() {
            requestEditorPanelFocus();
        }

        private void setNonFocusable(JTabbedPane tabbedPane) {
            // Set non-focusable, so that tab activation does not steal the focus form the included component.
            tabbedPane.setFocusable(false);
        }

        private void requestEditorPanelFocus() {
            Framework.getInstance().getMainWindow().requestFocus(editorPanel);
        }
    }

    public EditorPanelDockable(GraphEditorPanel editorPanel, String title, String persistentID) {
        super(new ContentPanel(title, editorPanel, ContentPanel.CLOSE_BUTTON | ContentPanel.MAXIMIZE_BUTTON),
                persistentID);

        addTabListener(new FocusDockableListener(editorPanel));
    }

}
