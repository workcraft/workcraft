package org.workcraft.gui.tabs;

import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.AbstractDockable;
import org.flexdock.docking.event.DockingEvent;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.actions.ScriptedActionListener;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DockableWindow extends AbstractDockable {

    private final DockableWindowContentPanel panel;
    private final LinkedList<Component> dragSources = new LinkedList<>();
    private final MainWindow mainWindow;
    private boolean inTab = false;
    private boolean closed = false;

    private final ArrayList<DockableWindowTabListener> tabListeners = new ArrayList<>();

    private final ChangeListener tabChangeListener = e -> {
        if (e.getSource() instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            int myTabIndex = getTabIndex(tabbedPane, this);
            if (tabbedPane.getSelectedIndex() == myTabIndex) {
                for (DockableWindowTabListener l: tabListeners) {
                    l.tabSelected(tabbedPane, myTabIndex);
                }
            } else {
                for (DockableWindowTabListener l: tabListeners) {
                    l.tabDeselected(tabbedPane, myTabIndex);
                }
            }
        }
    };

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
                    for (DockableWindowTabListener l: tabListeners) {
                        l.headerClicked();
                    }
                }
            }
        });
    }

    public void addTabListener(DockableWindowTabListener listener) {
        tabListeners.add(listener);
    }

    public void clearTabListeners() {
        tabListeners.clear();
    }

    public boolean isMaximized() {
        return panel.isMaximized();
    }

    public void setMaximized(boolean maximized) {
        panel.setMaximized(maximized);
        updateHeaders(this.getDockingPort(), mainWindow.getDefaultActionListener());
        if (maximized) {
            for (DockableWindowTabListener l: tabListeners) {
                l.windowMaximised();
            }
        } else {
            for (DockableWindowTabListener l: tabListeners) {
                l.windowRestored();
            }
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public Component getComponent() {
        return panel;
    }

    public DockableWindowContentPanel getContentPanel() {
        return panel;
    }

    public static int getTabIndex(JTabbedPane tabbedPane, DockableWindow window) {
        int myTabIndex = -1;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getComponentAt(i) == window.getComponent()) {
                myTabIndex = i;
                break;
            }
        }
        return myTabIndex;
    }

    public static void updateHeaders(DockingPort port, ScriptedActionListener actionListener) {
        for (Object d: port.getDockables()) {
            DockableWindow dockable = (DockableWindow) d;
            boolean inTab = dockable.getComponent().getParent() instanceof JTabbedPane;
            DockableWindowContentPanel contentPanel = dockable.getContentPanel();
            if (!inTab || dockable.isMaximized()) {
                contentPanel.setHeaderVisible(true);
            } else {
                contentPanel.setHeaderVisible(false);
                JTabbedPane tabbedPane = (JTabbedPane) dockable.getComponent().getParent();
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if (dockable.getComponent() == tabbedPane.getComponentAt(i)) {
                        DockableTab dockableTab = new DockableTab(dockable, actionListener);
                        tabbedPane.setTabComponentAt(i, dockableTab);
                        break;
                    }
                }
            }
        }
    }

    public void setTitle(String title) {
        if (!getTitle().equals(title)) {
            getContentPanel().setTitle(title);
            setTabText(title);
        }
    }

    public String getTitle() {
        return getContentPanel().getTitle();
    }

    public int getID() {
        return getContentPanel().getID();
    }

    private static void processTabEvents(DockingPort port) {
        for (Object d: port.getDockables()) {
            DockableWindow dockable = (DockableWindow) d;
            dockable.processTabEvents();
        }
    }

    public void processTabEvents() {
        Container parent = getComponent().getParent();
        if (parent instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) parent;
            if (!inTab) {
                inTab = true;
                for (DockableWindowTabListener l: tabListeners) {
                    l.dockedInTab(tabbedPane, getTabIndex(tabbedPane, this));
                }
            }
            List<ChangeListener> tabbedPaneListeners = Arrays.asList(tabbedPane.getChangeListeners());
            if (!tabbedPaneListeners.contains(tabChangeListener)) {
                tabbedPane.addChangeListener(tabChangeListener);
            }
        } else if (inTab) {
            inTab = false;
            for (DockableWindowTabListener l: tabListeners) {
                l.dockedStandalone();
            }
        }
    }

    @Override
    public void dockingComplete(DockingEvent evt) {
        processTabEvents(evt.getNewDockingPort());
        updateHeaders(evt.getNewDockingPort(), mainWindow.getDefaultActionListener());
        super.dockingComplete(evt);
    }

    @Override
    public void undockingComplete(DockingEvent evt) {
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

}
