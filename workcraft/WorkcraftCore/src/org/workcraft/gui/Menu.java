package org.workcraft.gui;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionCheckBoxMenuItem;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.tabs.UtilityPanelDockable;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.PluginManager;
import org.workcraft.utils.CommandUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Menu extends JMenuBar {

    private final JMenu mnImport = new JMenu("Import");
    private final JMenu mnExport = new JMenu("Export");
    private final JMenu mnRecent = new JMenu("Open recent");
    private final JMenu mnToolbars = new JMenu("Toolbars");
    private final JMenu mnUtilityPanels = new JMenu("Utility panels");
    private final Map<JToolBar, ActionCheckBoxMenuItem> toolbarItems = new HashMap<>();
    private final Map<UtilityPanelDockable, ActionCheckBoxMenuItem> utilityPanelItemMap = new HashMap<>();
    private final List<JMenu> mnCommandsMenus = new ArrayList<>();
    private final JMenu mnHelp = new JMenu("Help");

    Menu() {
        super();
        addFileMenu();
        addEditMenu();
        addViewMenu();
        addHelpMenu();
        setCommandsMenus(null);
    }

    private void addFileMenu() {
        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic(KeyEvent.VK_F);
        add(mnFile);

        ActionMenuItem miNewModel = new ActionMenuItem(MainWindowActions.CREATE_WORK_ACTION);
        miNewModel.setMnemonic(KeyEvent.VK_N);
        mnFile.add(miNewModel);

        ActionMenuItem miOpenModel = new ActionMenuItem(MainWindowActions.OPEN_WORK_ACTION);
        miOpenModel.setMnemonic(KeyEvent.VK_O);
        mnFile.add(miOpenModel);
        mnFile.add(mnRecent);

        ActionMenuItem miMergeModel = new ActionMenuItem(MainWindowActions.MERGE_WORK_ACTION);
        miMergeModel.setMnemonic(KeyEvent.VK_M);
        mnFile.add(miMergeModel);

        mnFile.addSeparator();

        ActionMenuItem miSaveWork = new ActionMenuItem(MainWindowActions.SAVE_WORK_ACTION);
        miSaveWork.setMnemonic(KeyEvent.VK_S);
        mnFile.add(miSaveWork);

        ActionMenuItem miSaveWorkAs = new ActionMenuItem(MainWindowActions.SAVE_WORK_AS_ACTION);
        mnFile.add(miSaveWorkAs);

        ActionMenuItem miCloseActive = new ActionMenuItem(MainWindowActions.CLOSE_ACTIVE_EDITOR_ACTION);
        mnFile.add(miCloseActive);

        ActionMenuItem miCloseAll = new ActionMenuItem(MainWindowActions.CLOSE_ALL_EDITORS_ACTION);
        mnFile.add(miCloseAll);

        mnFile.addSeparator();

        mnFile.add(mnImport);
        setImportMenu();

        mnFile.add(mnExport);

        mnFile.addSeparator();

        // FIXME: Workspace functionality is not working yet.
/*
        ActionMenuItem miNewWorkspace = new ActionMenuItem(WorkspaceWindowActions.NEW_WORKSPACE_AS_ACTION);
        mnFile.add(miNewWorkspace);

        ActionMenuItem miOpenWorkspace = new ActionMenuItem(WorkspaceWindowActions.OPEN_WORKSPACE_ACTION);
        mnFile.add(miOpenWorkspace);

        ActionMenuItem miAddFiles = new ActionMenuItem(WorkspaceWindowActions.ADD_FILES_TO_WORKSPACE_ACTION);
        mnFile.add(miAddFiles);

        ActionMenuItem miSaveWorkspace = new ActionMenuItem(WorkspaceWindowActions.SAVE_WORKSPACE_ACTION);
        mnFile.add(miSaveWorkspace);

        ActionMenuItem miSaveWorkspaceAs = new ActionMenuItem(WorkspaceWindowActions.SAVE_WORKSPACE_AS_ACTION);
        mnFile.add(miSaveWorkspaceAs);

        mnFile.addSeparator();
*/
        ActionMenuItem miShutdownGUI = new ActionMenuItem(MainWindowActions.SHUTDOWN_GUI_ACTION);
        mnFile.add(miShutdownGUI);
        mnFile.addSeparator();

        ActionMenuItem miExit = new ActionMenuItem(MainWindowActions.EXIT_ACTION);
        mnFile.add(miExit);
    }

    private void addEditMenu() {
        JMenu mnEdit = new JMenu("Edit");
        mnEdit.setMnemonic(KeyEvent.VK_E);
        add(mnEdit);

        ActionMenuItem miUndo = new ActionMenuItem(MainWindowActions.EDIT_UNDO_ACTION);
        miUndo.setMnemonic(KeyEvent.VK_U);
        mnEdit.add(miUndo);

        ActionMenuItem miRedo = new ActionMenuItem(MainWindowActions.EDIT_REDO_ACTION);
        miRedo.setMnemonic(KeyEvent.VK_R);
        mnEdit.add(miRedo);

        mnEdit.addSeparator();

        ActionMenuItem miCut = new ActionMenuItem(MainWindowActions.EDIT_CUT_ACTION);
        miCut.setMnemonic(KeyEvent.VK_T);
        mnEdit.add(miCut);

        ActionMenuItem miCopy = new ActionMenuItem(MainWindowActions.EDIT_COPY_ACTION);
        miCopy.setMnemonic(KeyEvent.VK_C);
        mnEdit.add(miCopy);

        ActionMenuItem miPaste = new ActionMenuItem(MainWindowActions.EDIT_PASTE_ACTION);
        miPaste.setMnemonic(KeyEvent.VK_P);
        mnEdit.add(miPaste);

        ActionMenuItem miDelete = new ActionMenuItem(MainWindowActions.EDIT_DELETE_ACTION);
        miDelete.setMnemonic(KeyEvent.VK_D);
        // Add Backspace as an alternative shortcut for delete action (in addition to the Delete key).
        InputMap deleteInputMap = miDelete.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke backspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
        deleteInputMap.put(backspace, MainWindowActions.EDIT_DELETE_ACTION);
        miDelete.getActionMap().put(MainWindowActions.EDIT_DELETE_ACTION, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindowActions.EDIT_DELETE_ACTION.run();
            }
        });
        mnEdit.add(miDelete);

        mnEdit.addSeparator();

        ActionMenuItem miSelectAll = new ActionMenuItem(MainWindowActions.EDIT_SELECT_ALL_ACTION);
        miSelectAll.setMnemonic(KeyEvent.VK_A);
        mnEdit.add(miSelectAll);

        ActionMenuItem miSelectInverse = new ActionMenuItem(MainWindowActions.EDIT_SELECT_INVERSE_ACTION);
        miSelectInverse.setMnemonic(KeyEvent.VK_V);
        mnEdit.add(miSelectInverse);

        ActionMenuItem miSelectNone = new ActionMenuItem(MainWindowActions.EDIT_SELECT_NONE_ACTION);
        miSelectNone.setMnemonic(KeyEvent.VK_E);
        mnEdit.add(miSelectNone);

        mnEdit.addSeparator();

        ActionMenuItem miProperties = new ActionMenuItem(MainWindowActions.EDIT_SETTINGS_ACTION);
        mnEdit.add(miProperties);
    }

    private void addViewMenu() {
        JMenu mnView = new JMenu("View");
        mnView.setMnemonic(KeyEvent.VK_V);
        add(mnView);

        ActionMenuItem miZoomIn = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_IN);
        mnView.add(miZoomIn);

        ActionMenuItem miZoomOut = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_OUT);
        mnView.add(miZoomOut);

        ActionMenuItem miZoomDefault = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_DEFAULT);
        mnView.add(miZoomDefault);

        ActionMenuItem miZoomFit = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_FIT);
        mnView.add(miZoomFit);

        mnView.addSeparator();

        ActionMenuItem miPanCenter = new ActionMenuItem(MainWindowActions.VIEW_PAN_CENTER);
        mnView.add(miPanCenter);

        ActionMenuItem miPanLeft = new ActionMenuItem(MainWindowActions.VIEW_PAN_LEFT);
        mnView.add(miPanLeft);

        ActionMenuItem miPanUp = new ActionMenuItem(MainWindowActions.VIEW_PAN_UP);
        mnView.add(miPanUp);

        ActionMenuItem miPanRight = new ActionMenuItem(MainWindowActions.VIEW_PAN_RIGHT);
        mnView.add(miPanRight);

        ActionMenuItem miPanDown = new ActionMenuItem(MainWindowActions.VIEW_PAN_DOWN);
        mnView.add(miPanDown);

        mnView.addSeparator();

        mnView.add(mnToolbars);
        mnView.add(mnUtilityPanels);

        ActionMenuItem miResetLayout = new ActionMenuItem(MainWindowActions.RESET_GUI_ACTION);
        mnView.add(miResetLayout);
    }

    private void addHelpMenu() {
        mnHelp.setText("Help");
        mnHelp.setMnemonic(KeyEvent.VK_H);
        add(mnHelp);

        ActionMenuItem miOverview = new ActionMenuItem(MainWindowActions.HELP_OVERVIEW_ACTION);
        mnHelp.add(miOverview);

        ActionMenuItem miContents = new ActionMenuItem(MainWindowActions.HELP_CONTENTS_ACTION);
        mnHelp.add(miContents);

        ActionMenuItem miTutorials = new ActionMenuItem(MainWindowActions.HELP_TUTORIALS_ACTION);
        mnHelp.add(miTutorials);

        mnHelp.addSeparator();

        ActionMenuItem miBugreport = new ActionMenuItem(MainWindowActions.HELP_BUGREPORT_ACTION);
        mnHelp.add(miBugreport);

        mnHelp.addSeparator();

        ActionMenuItem miAbout = new ActionMenuItem(MainWindowActions.HELP_ABOUT_ACTION);
        mnHelp.add(miAbout);
    }

    private void setImportMenu() {
        mnImport.removeAll();
        mnImport.setEnabled(false);

        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        PluginManager pluginManager = framework.getPluginManager();
        for (Importer importer : pluginManager.getSortedImporters()) {
            Format format = importer.getFormat();
            String text = format.getDescription() + " (*" + format.getExtension() + ")";
            Action action = new Action(text, () -> mainWindow.importFrom(importer));
            ActionMenuItem miImport = new ActionMenuItem(action);
            mnImport.add(miImport);
            mnImport.setEnabled(true);
        }
        revalidate();
    }

    private void setExportMenu(final WorkspaceEntry we) {
        mnExport.removeAll();
        mnExport.setEnabled(false);
        if (we != null) {
            addExporters(mnExport, we);
        }
    }

    public void setExportMenuState(boolean enable) {
        mnExport.setEnabled(enable);
    }

    public final void registerToolbar(JToolBar toolbar) {
        Action action = new Action(toolbar.getName(), () -> toolbar.setVisible(!toolbar.isVisible()));
        ActionCheckBoxMenuItem miToolbarItem = new ActionCheckBoxMenuItem(action);
        miToolbarItem.setSelected(toolbar.isVisible());
        toolbarItems.put(toolbar, miToolbarItem);
        mnToolbars.add(miToolbarItem);
    }

    public final void registerUtilityPanelDockable(UtilityPanelDockable utilityPanelDockable) {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        Action action = new Action(utilityPanelDockable.getTitle(), () -> mainWindow.toggleUtilityPanelDockable(utilityPanelDockable));
        ActionCheckBoxMenuItem menuItem = new ActionCheckBoxMenuItem(action);
        menuItem.setSelected(!utilityPanelDockable.isClosed());
        utilityPanelItemMap.put(utilityPanelDockable, menuItem);
        mnUtilityPanels.add(menuItem);
    }

    public final void updateRecentMenu() {
        ArrayList<String> entries = Framework.getInstance().getRecentFilePaths();
        mnRecent.removeAll();
        mnRecent.setEnabled(false);
        int index = 0;
        final MainWindow mainWindow = Framework.getInstance().getMainWindow();
        for (final String entry : entries) {
            if (entry != null) {
                JMenuItem miFile = new JMenuItem();
                if (index > 9) {
                    miFile.setText(entry);
                } else {
                    miFile.setText(index + ". " + entry);
                    miFile.setMnemonic(index + '0');
                    index++;
                }
                miFile.addActionListener(event -> mainWindow.openWork(new File(entry)));
                mnRecent.add(miFile);
                mnRecent.setEnabled(true);
            }
        }
        mnRecent.addSeparator();
        JMenuItem miClear = new JMenuItem("Clear the list");
        miClear.addActionListener(event -> {
            Framework framework = Framework.getInstance();
            framework.clearRecentFilePaths();
            updateRecentMenu();
        });
        mnRecent.add(miClear);
    }

    public final void setToolbarVisibility(JToolBar toolbar, boolean selected) {
        ActionCheckBoxMenuItem mi = toolbarItems.get(toolbar);
        if (mi != null) {
            mi.setSelected(selected);
        }
    }

    public final void setUtilityPanelDockableVisibility(UtilityPanelDockable utilityPanelDockable, boolean selected) {
        ActionCheckBoxMenuItem mi = utilityPanelItemMap.get(utilityPanelDockable);
        if (mi != null) {
            mi.setSelected(selected);
        }
    }

    public void setCommandsMenus(final WorkspaceEntry we) {
        for (JMenu commandsMenu1 : mnCommandsMenus) {
            remove(commandsMenu1);
        }
        mnCommandsMenus.clear();
        remove(mnHelp);
        List<Command> applicableVisibleCommands = CommandUtils.getApplicableVisibleCommands(we);
        List<String> orderedCategoryNames = CommandUtils.getOrderedCategoryNames(applicableVisibleCommands);
        for (String categoryName : orderedCategoryNames) {
            JMenu menu = CommandUtils.createCommandsMenu(categoryName, applicableVisibleCommands);
            add(menu);
            mnCommandsMenus.add(menu);
        }
        add(mnHelp);
        revalidate();
    }

    public void updateCommandsMenuState(boolean enable) {
        for (JMenu commandsMenu : mnCommandsMenus) {
            commandsMenu.setEnabled(enable);
        }
    }

    public void setMenuForWorkspaceEntry(final WorkspaceEntry we) {
        setExportMenu(we);
        setCommandsMenus(we);
        if (we != null) {
            we.updateActionState();
        }
    }

    public static void addExporters(JMenu menu, WorkspaceEntry we) {
        VisualModel model = we.getModelEntry().getVisualModel();
        PluginManager pm = Framework.getInstance().getPluginManager();
        List<Exporter> exporters = pm.getSortedExporters();

        boolean hasVisualModelExporter = false;
        for (Exporter exporter : exporters) {
            if (exporter.isCompatible(model)) {
                addMenuItem(menu, exporter, we);
                hasVisualModelExporter = true;
            }
        }

        boolean hasMathModelExporter = false;
        for (Exporter exporter : exporters) {
            if (exporter.isCompatible(model.getMathModel())) {
                if (hasVisualModelExporter && !hasMathModelExporter) {
                    menu.addSeparator();
                }
                addMenuItem(menu, exporter, we);
                hasMathModelExporter = true;
            }
        }
        menu.revalidate();
    }

    private static void addMenuItem(JMenu menu, Exporter exporter, WorkspaceEntry we) {
        menu.add(getMenuItem(exporter, we));
        menu.setEnabled(true);
    }

    private static JMenuItem getMenuItem(Exporter exporter, WorkspaceEntry we) {
        Format format = exporter.getFormat();
        String text = format.getDescription() + " (*" + format.getExtension() + ")";
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        Action action = new Action(text, () -> mainWindow.export(exporter, we));
        return new ActionMenuItem(action);
    }

}
