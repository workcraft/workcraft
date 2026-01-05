package org.workcraft.gui.tabs;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class DockingUtils {

    public static DockableWindow createPlaceholderDockable(JComponent component, String title, DockingPort port) {
        ContentPanel panel = new ContentPanel(title, component, 0);
        DockableWindow dockable = new DockableWindow(panel, title);
        DockingManager.registerDockable(dockable);
        DockingManager.dock(dockable, port, DockingConstants.CENTER_REGION);
        return dockable;
    }

    public static DockableWindow createEditorDockable(JComponent component, String title, Dockable neighbour,
            String persistentID) {

        return createDockable(component, title, neighbour,
                ContentPanel.CLOSE_BUTTON | ContentPanel.MAXIMIZE_BUTTON,
                DockingConstants.CENTER_REGION, -1, persistentID);
    }

    public static DockableWindow createUtilityDockable(JComponent component, String title, Dockable neighbour) {
        return createUtilityDockable(component, title, neighbour,
                DockingConstants.CENTER_REGION, -1);
    }

    public static DockableWindow createUtilityDockable(JComponent component, String title, Dockable neighbour,
            String region, float split) {

        return createDockable(component, title, neighbour,
                ContentPanel.CLOSE_BUTTON, region, split, title);
    }

    private static DockableWindow createDockable(JComponent component, String title, Dockable neighbour, int options,
            String region, float split, String persistentID) {

        ContentPanel panel = new ContentPanel(title, component, options);
        DockableWindow dockable = new DockableWindow(panel, persistentID);
        DockingManager.registerDockable(dockable);
        DockingManager.dock(dockable, neighbour, region);
        if (split > 0) {
            DockingManager.setSplitProportion(dockable, split);
        }
        return dockable;
    }

    public static void unmaximise(Collection<DockableWindow> dockableWindows) {
        for (DockableWindow dockableWindow : dockableWindows) {
            if (dockableWindow.isMaximized()) {
                DockingManager.toggleMaximized(dockableWindow);
                dockableWindow.setMaximized(false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<DockableWindow> getDockableWindows(DockingPort dockingPort) {
        return dockingPort.getDockables();
    }

    public static void processTabEvents(DockingPort dockingPort) {
        for (DockableWindow dockableWindow : getDockableWindows(dockingPort)) {
            dockableWindow.processTabEvents();
        }
    }

    public static void updateHeaders(DockingPort dockingPort) {
        for (DockableWindow dockableWindow : getDockableWindows(dockingPort)) {
            updateHeader(dockableWindow);
        }
    }

    public static void updateHeader(DockableWindow dockableWindow) {
        Container parent = dockableWindow.getComponent().getParent();
        boolean inTab = parent instanceof JTabbedPane;
        if (!inTab || dockableWindow.isMaximized()) {
            dockableWindow.setHeaderVisible(true);
        } else {
            dockableWindow.setHeaderVisible(false);
            JTabbedPane tabbedPane = (JTabbedPane) parent;
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (dockableWindow.getComponent() == tabbedPane.getComponentAt(i)) {
                    DockableTab dockableTab = new DockableTab(dockableWindow);
                    tabbedPane.setTabComponentAt(i, dockableTab);
                    break;
                }
            }
        }
    }

    public static void closeTab(JTabbedPane tabbedPane, int tabIndex) {
        if ((tabIndex >= 0) && (tabIndex < tabbedPane.getTabCount())) {
            Component component = tabbedPane.getComponentAt(tabIndex);
            if (component instanceof ContentPanel contentPanel) {
                MainWindow mainWindow = Framework.getInstance().getMainWindow();
                mainWindow.closeDockableWindow(contentPanel.getDockableWindow());
            }
        }
    }

    public static int getTabIndex(JTabbedPane tabbedPane, ContentPanel contentPanel) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getComponentAt(i) == contentPanel) {
                return i;
            }
        }
        return -1;
    }

    public static void activateNextTab(DockableWindow dockableWindow, int step) {
        if (dockableWindow != null) {
            Container parent = dockableWindow.getComponent().getParent();
            if (parent instanceof JTabbedPane tabbedPane) {
                int index = tabbedPane.getSelectedIndex();
                if (index >= 0) {
                    int nextIndex = Math.floorMod(index + step, tabbedPane.getTabCount());
                    tabbedPane.setSelectedIndex(nextIndex);
                }
            }
        }
    }

}
