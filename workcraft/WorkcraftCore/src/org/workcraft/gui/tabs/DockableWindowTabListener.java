package org.workcraft.gui.tabs;

import javax.swing.JTabbedPane;

public interface DockableWindowTabListener {
    void dockedInTab(JTabbedPane pane, int index);
    void dockedStandalone();
    void tabSelected(JTabbedPane pane, int index);
    void tabDeselected(JTabbedPane pane, int index);
    void headerClicked();
    void windowMaximised();
    void windowRestored();
}
