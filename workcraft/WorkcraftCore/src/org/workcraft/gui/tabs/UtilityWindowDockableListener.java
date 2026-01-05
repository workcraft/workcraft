package org.workcraft.gui.tabs;

import javax.swing.*;

public class UtilityWindowDockableListener implements DockableListener {

    private final Runnable updater;

    public UtilityWindowDockableListener(Runnable updater) {
        this.updater = updater;
    }

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
