package org.workcraft.gui.tabs;

import javax.swing.*;

public interface DockableListener {
    void dockedInTab(JTabbedPane pane, int index);
    void dockedStandalone();
    void tabSelected(JTabbedPane pane, int index);
    void headerClicked(int button);
    void windowMaximised();
    void windowRestored();
}
