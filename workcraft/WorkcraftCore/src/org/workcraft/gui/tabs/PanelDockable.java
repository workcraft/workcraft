package org.workcraft.gui.tabs;

import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.AbstractDockable;
import org.flexdock.docking.event.DockingEvent;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

public class PanelDockable extends AbstractDockable {

    private static final MouseAdapter TAB_MOUSE_LISTENER = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if ((e.getButton() == MouseEvent.BUTTON2) && (e.getSource() instanceof JTabbedPane tabbedPane)) {
                TabbedPaneUI ui = tabbedPane.getUI();
                int tabIndex = ui.tabForCoordinate(tabbedPane, e.getX(), e.getY());
                DockingUtils.closeTab(tabbedPane, tabIndex);
            }
        }
    };

    private final ContentPanel contentPanel;
    private final List<DockableListener> dockableListeners = new ArrayList<>();
    private final List<Component> dragSources = new ArrayList<>();

    private final ChangeListener tabChangeListener = e -> {
        if (e.getSource() instanceof JTabbedPane tabbedPane) {
            int tabIndex = DockingUtils.getTabIndex(tabbedPane, getComponent());
            for (DockableListener l : new ArrayList<>(dockableListeners)) {
                if (tabbedPane.getSelectedIndex() == tabIndex) {
                    l.tabSelected(tabbedPane, tabIndex);
                }
            }
        }
    };

    private boolean inTab = false;
    private boolean closed = false;

    public PanelDockable(ContentPanel contentPanel, String persistentID) {
        super(persistentID);
        this.contentPanel = contentPanel;
        contentPanel.setDockableWindow(this);
        setTabText(contentPanel.getTitle());

        Component header = contentPanel.getHeader();
        dragSources.add(contentPanel);
        dragSources.add(header);

        header.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON2) {
                        MainWindow mainWindow = Framework.getInstance().getMainWindow();
                        mainWindow.closePanelDockable(PanelDockable.this);
                    } else {
                        for (DockableListener l : new ArrayList<>(dockableListeners)) {
                            l.headerClicked(e.getButton());
                        }
                    }
                }
            });
    }

    @Override
    public ContentPanel getComponent() {
        return contentPanel;
    }

    public void addTabListener(DockableListener listener) {
        dockableListeners.add(listener);
    }

    public void clearTabListeners() {
        dockableListeners.clear();
    }

    public boolean isMaximized() {
        return contentPanel.isMaximized();
    }

    public void setMaximized(boolean maximized) {
        contentPanel.setMaximized(maximized);
        DockingUtils.updateHeader(this);
        for (DockableListener l : new ArrayList<>(dockableListeners)) {
            if (maximized) {
                l.windowMaximised();
            } else {
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

    public void setHeaderVisible(boolean value) {
        contentPanel.setHeaderVisible(value);
    }

    public void setTitle(String title) {
        if (!getTitle().equals(title)) {
            contentPanel.setTitle(title);
            setTabText(title);
        }
    }

    public String getTitle() {
        return contentPanel.getTitle();
    }

    public void processTabEvents() {
        Container parent = contentPanel.getParent();
        if (parent instanceof JTabbedPane tabbedPane) {
            if (!inTab) {
                inTab = true;
                int tabIndex = DockingUtils.getTabIndex(tabbedPane, contentPanel);
                for (DockableListener l : new ArrayList<>(dockableListeners)) {
                    l.dockedInTab(tabbedPane, tabIndex);
                }
            }
            setTabMouseListener(tabbedPane);
            setTabChangeListener(tabbedPane);
        } else if (inTab) {
            inTab = false;
            for (DockableListener l : new ArrayList<>(dockableListeners)) {
                l.dockedStandalone();
            }
        }
    }

    private void setTabMouseListener(JTabbedPane tabbedPane) {
        Set<MouseListener> listeners = new HashSet<>(Arrays.asList(tabbedPane.getMouseListeners()));
        if (!listeners.contains(TAB_MOUSE_LISTENER)) {
            tabbedPane.addMouseListener(TAB_MOUSE_LISTENER);
        }
    }

    private void setTabChangeListener(JTabbedPane tabbedPane) {
        Set<ChangeListener> listeners = new HashSet<>(Arrays.asList(tabbedPane.getChangeListeners()));
        if (!listeners.contains(tabChangeListener)) {
            tabbedPane.addChangeListener(tabChangeListener);
        }
    }

    @Override
    public void dockingComplete(DockingEvent event) {
        DockingPort dockingPort = event.getNewDockingPort();
        DockingUtils.processTabEvents(dockingPort);
        DockingUtils.updateHeaders(dockingPort);
        super.dockingComplete(event);
    }

    @Override
    public void undockingComplete(DockingEvent event) {
        DockingPort dockingPort = event.getOldDockingPort();
        DockingUtils.processTabEvents(dockingPort);
        DockingUtils.updateHeaders(dockingPort);
        super.undockingComplete(event);
    }

    @Override
    public List<Component> getDragSources() {
        return dragSources;
    }

    public boolean isHiddenTab() {
        Container parent = contentPanel.getParent();
        if (parent instanceof JTabbedPane tabbedPane) {
            int tabIndex = DockingUtils.getTabIndex(tabbedPane, contentPanel);
            return (tabbedPane.getSelectedIndex() != tabIndex);
        }
        return false;
    }

}
