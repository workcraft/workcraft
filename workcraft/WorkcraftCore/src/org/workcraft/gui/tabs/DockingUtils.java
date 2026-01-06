package org.workcraft.gui.tabs;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.editor.GraphEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class DockingUtils {

    public static PanelDockable createPlaceholderDockable(JComponent component, String title, DockingPort port) {
        PanelDockable result = new PanelDockable(new ContentPanel(title, component, 0), title);
        DockingManager.registerDockable(result);
        DockingManager.dock(result, port, DockingConstants.CENTER_REGION);
        return result;
    }

    public static EditorPanelDockable createEditorDockable(GraphEditorPanel editorPanel,
            String title, Dockable neighbour, String persistentID) {

        EditorPanelDockable result = new EditorPanelDockable(editorPanel, title, persistentID);
        DockingManager.registerDockable(result);
        DockingManager.dock(result, neighbour, DockingConstants.CENTER_REGION);
        return result;
    }

    public static UtilityPanelDockable createUtilityDockable(JComponent component, String title, Dockable neighbour) {
        return createUtilityDockable(component, title, neighbour, DockingConstants.CENTER_REGION, -1);
    }

    public static UtilityPanelDockable createUtilityDockable(JComponent component, String title, Dockable neighbour,
            String region, float split) {

        UtilityPanelDockable result = new UtilityPanelDockable(component, title);
        DockingManager.registerDockable(result);
        DockingManager.dock(result, neighbour, region);
        if (split > 0) {
            DockingManager.setSplitProportion(result, split);
        }
        return result;
    }

    public static void unmaximise(Collection<EditorPanelDockable> editorPanelDockables) {
        for (EditorPanelDockable editorPanelDockable : editorPanelDockables) {
            if (editorPanelDockable.isMaximized()) {
                DockingManager.toggleMaximized(editorPanelDockable);
                editorPanelDockable.setMaximized(false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<PanelDockable> getPanelDockables(DockingPort dockingPort) {
        return dockingPort.getDockables();
    }

    public static void processTabEvents(DockingPort dockingPort) {
        for (PanelDockable panelDockable : getPanelDockables(dockingPort)) {
            panelDockable.processTabEvents();
        }
    }

    public static void updateHeaders(DockingPort dockingPort) {
        for (PanelDockable panelDockable : getPanelDockables(dockingPort)) {
            updateHeader(panelDockable);
        }
    }

    public static void updateHeader(PanelDockable panelDockable) {
        Container parent = panelDockable.getComponent().getParent();
        boolean inTab = parent instanceof JTabbedPane;
        if (!inTab || panelDockable.isMaximized()) {
            panelDockable.setHeaderVisible(true);
        } else {
            panelDockable.setHeaderVisible(false);
            JTabbedPane tabbedPane = (JTabbedPane) parent;
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (panelDockable.getComponent() == tabbedPane.getComponentAt(i)) {
                    DockableTab dockableTab = new DockableTab(panelDockable);
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
                mainWindow.closePanelDockable(contentPanel.getPanelDockable());
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

    public static void activateNextTab(PanelDockable panelDockable, int step) {
        if (panelDockable != null) {
            Container parent = panelDockable.getComponent().getParent();
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
