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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.AbstractDockable;
import org.flexdock.docking.event.DockingEvent;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.tabs.DockableTab;

public class DockableWindow extends AbstractDockable {
	private DockableWindowContentPanel panel;
	private LinkedList<Component> dragSources = new LinkedList<Component>();
	private MainWindow mainWindow;
	private boolean inTab = false;
	private boolean closed = false;
	private boolean tabEventsEnabled = false;

	private ArrayList<DockableWindowTabListener> tabListeners = new ArrayList<DockableWindowTabListener>();

	private ChangeListener tabChangeListener = new ChangeListener() {



		@Override
		public void stateChanged(ChangeEvent e) {
			JTabbedPane tabbedPane = (JTabbedPane)e.getSource();

			int myTabIndex = getTabIndex(tabbedPane, DockableWindow.this);

			if (tabbedPane.getSelectedIndex() == myTabIndex)
				for (DockableWindowTabListener l : tabListeners) {
					l.tabSelected(tabbedPane, myTabIndex);
				}
			else
				for (DockableWindowTabListener l : tabListeners)
				{
					l.tabDeselected(tabbedPane, myTabIndex);
				}
		}
	};

	public void addTabListener (DockableWindowTabListener listener)
	{
		tabListeners.add(listener);
	}

	public void removeTabChangeListener (DockableWindowTabListener listener)
	{
		tabListeners.remove(listener);
	}

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
		dragSources.add(panel.header);

		panel.header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					for (DockableWindowTabListener l : tabListeners) {
						l.headerClicked();
					}
				}
			}
		});
	}

	public Component getComponent() {
		return panel;
	}

	public DockableWindowContentPanel getContentPanel() {
		return panel;
	}

	public static int getTabIndex(JTabbedPane tabbedPane, DockableWindow window)
	{
		int myTabIndex = -2;

		for (int i=0; i<tabbedPane.getTabCount(); i++)
			if (tabbedPane.getComponentAt(i) == window.getComponent())
			{
				myTabIndex = i;
				break;
			}

		return myTabIndex;
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

	private static void processTabEvents(DockingPort port) {
		for (Object d : port.getDockables()) {
			DockableWindow dockable = (DockableWindow)d;
			dockable.processTabEvents();
		}
	}

	public void processTabEvents()
	{
		if (getComponent().getParent() instanceof JTabbedPane) {

			JTabbedPane tabbedPane = (JTabbedPane)getComponent().getParent();

			if (!inTab) {
				inTab = true;
				for (DockableWindowTabListener l : tabListeners)
					l.dockedInTab(tabbedPane, getTabIndex(tabbedPane, this));
			}

			if (!Arrays.asList(tabbedPane.getChangeListeners()).contains(tabChangeListener)) {
				tabbedPane.addChangeListener(tabChangeListener);
			}
		} else
		{
			if (inTab) {
				inTab = false;
				for (DockableWindowTabListener l : tabListeners)
					l.dockedStandalone();
			}
		}
	}

	@Override
	public void dockingComplete(DockingEvent evt) {
		//	System.out.println ("docked " + getTitle());
		processTabEvents(evt.getNewDockingPort());
		updateHeaders(evt.getNewDockingPort(), mainWindow.getDefaultActionListener());
		super.dockingComplete(evt);
	}



	@Override
	public void undockingComplete(DockingEvent evt) {
		//		System.out.println ("undocked " + getTitle());
		processTabEvents(evt.getOldDockingPort());
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

	public void setTabEventsEnabled(boolean tabEventsEnabled) {
		this.tabEventsEnabled = tabEventsEnabled;
	}
}
