/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;

import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.AbstractDockable;
import org.flexdock.docking.event.DockingEvent;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.tabs.DockableTab;

public class DockableWindow extends AbstractDockable {
	private DockableWindowContentPanel panel;
	private LinkedList<Component> dragSources = new LinkedList<Component>();
	private MainWindow mainWindow;
	private boolean closed = false;

	public boolean isMaximized() {
		return panel.isMaximized();
	}

	public void setMaximized(boolean maximized) {
		panel.setMaximized(maximized);
		updateHeaders(this.getDockingPort(), mainWindow.getDefaultActionListener());
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public DockableWindow(MainWindow mainWindow, DockableWindowContentPanel panel, String persistentID) {
		super(persistentID);
		this.panel = panel;
		this.mainWindow = mainWindow;
		setTabText(panel.getTitle());
		dragSources.add(panel);
	}

	public Component getComponent() {
		return panel;
	}

	public DockableWindowContentPanel getContentPanel() {
		return panel;
	}

	public static void updateHeaders(DockingPort port, ScriptedActionListener actionListener) {
		for (Object d : port.getDockables()) {
			DockableWindow dockable = (DockableWindow)d;

			boolean inTab = dockable.getComponent().getParent() instanceof JTabbedPane;

			if (inTab && !dockable.isMaximized()) {
				dockable.getContentPanel().setHeaderVisible(false);
				JTabbedPane tabbedPane = (JTabbedPane)dockable.getComponent().getParent();

				for (int i=0; i<tabbedPane.getComponentCount(); i++)
					if (dockable.getComponent() == tabbedPane.getComponentAt(i)) {
						tabbedPane.setTabComponentAt(i, new DockableTab(dockable, actionListener));
						break;
					}
			}
			else
				dockable.getContentPanel().setHeaderVisible(true);
		}
	}

	public String getTitle() {
		return panel.getTitle();
	}

	public int getID() {
		return panel.getID();
	}

	@Override
	public void dockingComplete(DockingEvent evt) {
//		System.out.println ("docked " + getTitle());
		updateHeaders(evt.getNewDockingPort(), mainWindow.getDefaultActionListener());
		super.dockingComplete(evt);

	}

	@Override
	public void undockingComplete(DockingEvent evt) {
//		System.out.println ("undocked " + getTitle());
		updateHeaders(evt.getOldDockingPort(), mainWindow.getDefaultActionListener());
		super.undockingComplete(evt);
	}

	@Override
	public List<Component> getDragSources() {
		return dragSources;
	}

	public int getOptions() {
		return panel.getOptions();
	}

}
