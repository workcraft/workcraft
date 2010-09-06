package org.workcraft.gui;

import javax.swing.JTabbedPane;

public interface DockableWindowTabListener {
	public void dockedInTab(JTabbedPane pane, int index);
	public void dockedStandalone();
	public void tabSelected(JTabbedPane pane, int index);
	public void tabDeselected(JTabbedPane pane, int index);
}
