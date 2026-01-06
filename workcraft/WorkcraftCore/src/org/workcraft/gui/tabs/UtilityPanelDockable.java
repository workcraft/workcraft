package org.workcraft.gui.tabs;

import javax.swing.*;

public class UtilityPanelDockable extends PanelDockable {

    private record UpdateDockableListener(Runnable updater) implements DockableListener {
        @Override
        public void tabSelected(JTabbedPane tabbedPane, int tabIndex) {
            updater.run();
        }

        @Override
        public void dockedStandalone() {
            updater.run();
        }

        @Override
        public void dockedInTab(JTabbedPane tabbedPane, int tabIndex) {
            updater.run();
        }

        @Override
        public void headerClicked(int button) {
        }

        @Override
        public void windowMaximised() {
        }

        @Override
        public void windowRestored() {
        }
    }

    public UtilityPanelDockable(JComponent content, String title) {
        super(new ContentPanel(title, content, ContentPanel.CLOSE_BUTTON), title);
    }

    public void registerUpdater(Runnable updater) {
        if (updater != null) {
            addTabListener(new UpdateDockableListener(updater));
        }
    }

}
