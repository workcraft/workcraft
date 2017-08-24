package org.workcraft.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.workcraft.Framework;
import org.workcraft.MenuOrdering.Position;
import org.workcraft.PluginManager;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.actions.ActionCheckBoxMenuItem;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.actions.CommandAction;
import org.workcraft.gui.actions.ExportAction;
import org.workcraft.gui.actions.ToggleToolbarAction;
import org.workcraft.gui.actions.ToggleWindowAction;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.util.Commands;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class MainMenu extends JMenuBar {
    private static final String MENU_SECTION_PROMOTED_PREFIX = "!";

    private final MainWindow mainWindow;
    private final JMenu mnExport = new JMenu("Export");
    private final JMenu mnRecent = new JMenu("Open recent");
    private final JMenu mnToolbars = new JMenu("Toolbars");
    private final JMenu mnWindows = new JMenu("Windows");
    private final HashMap<JToolBar, ActionCheckBoxMenuItem> toolbarItems = new HashMap<>();
    private final HashMap<Integer, ActionCheckBoxMenuItem> windowItems = new HashMap<>();
    private final LinkedList<JMenu> mnCommandsList = new LinkedList<>();
    private final JMenu mnHelp = new JMenu("Help");

    MainMenu(final MainWindow mainWindow) {
        super();
        this.mainWindow = mainWindow;
        addFileMenu();
        addEditMenu();
        addViewMenu();
        addHelpMenu();
    }

    private void addFileMenu() {
        JMenu mnFile = new JMenu("File");

        ActionMenuItem miNewModel = new ActionMenuItem(MainWindowActions.CREATE_WORK_ACTION);
        miNewModel.setMnemonic(KeyEvent.VK_N);
        miNewModel.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miOpenModel = new ActionMenuItem(MainWindowActions.OPEN_WORK_ACTION);
        miOpenModel.setMnemonic(KeyEvent.VK_O);
        miOpenModel.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miMergeModel = new ActionMenuItem(MainWindowActions.MERGE_WORK_ACTION);
        miMergeModel.setMnemonic(KeyEvent.VK_M);
        miMergeModel.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miSaveWork = new ActionMenuItem(MainWindowActions.SAVE_WORK_ACTION);
        miSaveWork.setMnemonic(KeyEvent.VK_S);
        miSaveWork.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miSaveWorkAs = new ActionMenuItem(MainWindowActions.SAVE_WORK_AS_ACTION);
        miSaveWorkAs.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miCloseAll = new ActionMenuItem(MainWindowActions.CLOSE_ALL_EDITORS_ACTION);
        miCloseAll.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miCloseActive = new ActionMenuItem(MainWindowActions.CLOSE_ACTIVE_EDITOR_ACTION);
        miCloseActive.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miImport = new ActionMenuItem(MainWindowActions.IMPORT_ACTION);
        miImport.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miNewWorkspace = new ActionMenuItem(WorkspaceWindow.Actions.NEW_WORKSPACE_AS_ACTION);
        miNewWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miOpenWorkspace = new ActionMenuItem(WorkspaceWindow.Actions.OPEN_WORKSPACE_ACTION);
        miOpenWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miAddFiles = new ActionMenuItem(WorkspaceWindow.Actions.ADD_FILES_TO_WORKSPACE_ACTION);
        miAddFiles.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miSaveWorkspace = new ActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_ACTION);
        miSaveWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miSaveWorkspaceAs = new ActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_AS_ACTION);
        miSaveWorkspaceAs.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miShutdownGUI = new ActionMenuItem(MainWindowActions.SHUTDOWN_GUI_ACTION);
        miShutdownGUI.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miExit = new ActionMenuItem(MainWindowActions.EXIT_ACTION);
        miExit.addScriptedActionListener(mainWindow.getDefaultActionListener());

        mnFile.add(miNewModel);
        mnFile.add(miOpenModel);
        mnFile.add(mnRecent);
        mnFile.add(miMergeModel);

        mnFile.addSeparator();
        mnFile.add(miSaveWork);
        mnFile.add(miSaveWorkAs);
        mnFile.add(miCloseActive);
        mnFile.add(miCloseAll);

        mnFile.addSeparator();
        mnFile.add(miImport);
        mnFile.add(mnExport);

// FIXME: Workspace functionality is not working yet.
//        mnFile.addSeparator();
//        mnFile.add(miNewWorkspace);
//        mnFile.add(miOpenWorkspace);
//        mnFile.add(miAddFiles);
//        mnFile.add(miSaveWorkspace);
//        mnFile.add(miSaveWorkspaceAs);

        mnFile.addSeparator();
        mnFile.add(miShutdownGUI);
        mnFile.addSeparator();
        mnFile.add(miExit);

        add(mnFile);
    }

    private void addExportSeparator(String text) {
        mnExport.add(new JLabel(text));
        mnExport.addSeparator();
    }

    private void addExporter(Exporter exporter) {
        Format format = exporter.getFormat();
        String text = format.getDescription() + " (*" + format.getExtension() + ")";
        ActionMenuItem miExport = new ActionMenuItem(new ExportAction(exporter), text);

        miExport.addScriptedActionListener(mainWindow.getDefaultActionListener());
        mnExport.add(miExport);
        mnExport.setEnabled(true);
    }

    private void addEditMenu() {
        JMenu mnEdit = new JMenu("Edit");

        ActionMenuItem miUndo = new ActionMenuItem(MainWindowActions.EDIT_UNDO_ACTION);
        miUndo.setMnemonic(KeyEvent.VK_U);
        miUndo.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miRedo = new ActionMenuItem(MainWindowActions.EDIT_REDO_ACTION);
        miRedo.setMnemonic(KeyEvent.VK_R);
        miRedo.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miCut = new ActionMenuItem(MainWindowActions.EDIT_CUT_ACTION);
        miCut.setMnemonic(KeyEvent.VK_T);
        miCut.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miCopy = new ActionMenuItem(MainWindowActions.EDIT_COPY_ACTION);
        miCopy.setMnemonic(KeyEvent.VK_C);
        miCopy.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miPaste = new ActionMenuItem(MainWindowActions.EDIT_PASTE_ACTION);
        miPaste.setMnemonic(KeyEvent.VK_P);
        miPaste.addScriptedActionListener(mainWindow.getDefaultActionListener());

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
        miDelete.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miSelectAll = new ActionMenuItem(MainWindowActions.EDIT_SELECT_ALL_ACTION);
        miSelectAll.setMnemonic(KeyEvent.VK_A);
        miSelectAll.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miSelectInverse = new ActionMenuItem(MainWindowActions.EDIT_SELECT_INVERSE_ACTION);
        miSelectInverse.setMnemonic(KeyEvent.VK_V);
        miSelectInverse.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miSelectNone = new ActionMenuItem(MainWindowActions.EDIT_SELECT_NONE_ACTION);
        miSelectNone.setMnemonic(KeyEvent.VK_E);
        miSelectNone.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miProperties = new ActionMenuItem(MainWindowActions.EDIT_SETTINGS_ACTION);
        miProperties.addScriptedActionListener(mainWindow.getDefaultActionListener());

        mnEdit.add(miUndo);
        mnEdit.add(miRedo);
        mnEdit.addSeparator();
        mnEdit.add(miCut);
        mnEdit.add(miCopy);
        mnEdit.add(miPaste);
        mnEdit.add(miDelete);
        mnEdit.addSeparator();
        mnEdit.add(miSelectAll);
        mnEdit.add(miSelectInverse);
        mnEdit.add(miSelectNone);
        mnEdit.addSeparator();
        mnEdit.add(miProperties);

        add(mnEdit);
    }

    private void addViewMenu() {
        JMenu mnView = new JMenu("View");

        ActionMenuItem miZoomIn = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_IN);
        miZoomIn.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miZoomOut = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_OUT);
        miZoomOut.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miZoomDefault = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_DEFAULT);
        miZoomDefault.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miZoomFit = new ActionMenuItem(MainWindowActions.VIEW_ZOOM_FIT);
        miZoomFit.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miPanLeft = new ActionMenuItem(MainWindowActions.VIEW_PAN_LEFT);
        miPanLeft.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miPanUp = new ActionMenuItem(MainWindowActions.VIEW_PAN_UP);
        miPanUp.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miPanRight = new ActionMenuItem(MainWindowActions.VIEW_PAN_RIGHT);
        miPanRight.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miPanDown = new ActionMenuItem(MainWindowActions.VIEW_PAN_DOWN);
        miPanDown.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miPanCenter = new ActionMenuItem(MainWindowActions.VIEW_PAN_CENTER);
        miPanCenter.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miResetLayout = new ActionMenuItem(MainWindowActions.RESET_GUI_ACTION);
        miResetLayout.addScriptedActionListener(mainWindow.getDefaultActionListener());

        mnView.add(miZoomIn);
        mnView.add(miZoomOut);
        mnView.add(miZoomDefault);
        mnView.add(miZoomFit);
        mnView.addSeparator();
        mnView.add(miPanCenter);
        mnView.add(miPanLeft);
        mnView.add(miPanUp);
        mnView.add(miPanRight);
        mnView.add(miPanDown);
        mnView.addSeparator();
        mnView.add(mnToolbars);
        mnView.add(mnWindows);
        mnView.add(miResetLayout);

        add(mnView);
    }

    private void addHelpMenu() {
        //JMenu mnHelp = new JMenu();
        mnHelp.setText("Help");

        ActionMenuItem miOverview = new ActionMenuItem(MainWindowActions.HELP_OVERVIEW_ACTION);
        miOverview.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miContents = new ActionMenuItem(MainWindowActions.HELP_CONTENTS_ACTION);
        miContents.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miTutorials = new ActionMenuItem(MainWindowActions.HELP_TUTORIALS_ACTION);
        miTutorials.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miBugreport = new ActionMenuItem(MainWindowActions.HELP_BUGREPORT_ACTION);
        miBugreport.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miQuestion = new ActionMenuItem(MainWindowActions.HELP_EMAIL_ACTION);
        miQuestion.addScriptedActionListener(mainWindow.getDefaultActionListener());

        ActionMenuItem miAbout = new ActionMenuItem(MainWindowActions.HELP_ABOUT_ACTION);
        miAbout.addScriptedActionListener(mainWindow.getDefaultActionListener());

        mnHelp.add(miOverview);
        mnHelp.add(miContents);
        mnHelp.add(miTutorials);
        mnHelp.addSeparator();
        mnHelp.add(miBugreport);
        mnHelp.add(miQuestion);
        mnHelp.addSeparator();
        mnHelp.add(miAbout);

        //add(Box.createHorizontalGlue());
        add(mnHelp);

    }

    private void setExportMenu(final WorkspaceEntry we) {
        mnExport.removeAll();
        mnExport.setEnabled(false);

        VisualModel model = we.getModelEntry().getVisualModel();
        final Framework framework = Framework.getInstance();
        PluginManager pluginManager = framework.getPluginManager();
        Collection<PluginInfo<? extends Exporter>> plugins = pluginManager.getPlugins(Exporter.class);

        boolean hasVisualModelExporter = false;
        for (PluginInfo<? extends Exporter> info : plugins) {
            Exporter exporter = info.getSingleton();
            if (exporter.getCompatibility(model) > Exporter.NOT_COMPATIBLE) {
                if (!hasVisualModelExporter) {
                    addExportSeparator("Visual model");
                }
                addExporter(exporter);
                hasVisualModelExporter = true;
            }
        }

        boolean hasMathModelExporter = false;
        for (PluginInfo<? extends Exporter> info : plugins) {
            Exporter exporter = info.getSingleton();
            if (exporter.getCompatibility(model.getMathModel()) > Exporter.NOT_COMPATIBLE) {
                if (!hasMathModelExporter) {
                    addExportSeparator("Math model");
                }
                addExporter(exporter);
                hasMathModelExporter = true;
            }
        }
        revalidate();
    }

    public void setExportMenuState(boolean enable) {
        mnExport.setEnabled(enable);
    }

    public final void registerToolbar(JToolBar toolbar) {
        ActionCheckBoxMenuItem miToolbarItem = new ActionCheckBoxMenuItem(new ToggleToolbarAction(toolbar));
        miToolbarItem.addScriptedActionListener(mainWindow.getDefaultActionListener());
        miToolbarItem.setSelected(toolbar.isVisible());
        toolbarItems.put(toolbar, miToolbarItem);
        mnToolbars.add(miToolbarItem);
    }

    public final void registerUtilityWindow(DockableWindow window) {
        ActionCheckBoxMenuItem miWindowItem = new ActionCheckBoxMenuItem(new ToggleWindowAction(window));
        miWindowItem.addScriptedActionListener(mainWindow.getDefaultActionListener());
        miWindowItem.setSelected(!window.isClosed());
        windowItems.put(window.getID(), miWindowItem);
        mnWindows.add(miWindowItem);
    }

    public final void setRecentMenu(ArrayList<String> entries) {
        mnRecent.removeAll();
        mnRecent.setEnabled(false);
        int index = 0;
        Collections.reverse(entries);
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
                miFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mainWindow.openWork(new File(entry));
                    }
                });
                mnRecent.add(miFile);
                mnRecent.setEnabled(true);
            }
        }
        mnRecent.addSeparator();
        JMenuItem miClear = new JMenuItem("Clear the list");
        miClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindow.clearRecentFilesMenu();
            }
        });
        mnRecent.add(miClear);
    }

    public final void setToolbarVisibility(JToolBar toolbar, boolean selected) {
        ActionCheckBoxMenuItem mi = toolbarItems.get(toolbar);
        if (mi != null) {
            mi.setSelected(selected);
        }
    }

    public final void setWindowVisibility(int id, boolean selected) {
        ActionCheckBoxMenuItem mi = windowItems.get(id);
        if (mi != null) {
            mi.setSelected(selected);
        }
    }

    private void createCommandsMenu(final WorkspaceEntry we) {
        removeCommandsMenu();

        List<Command> applicableCommands = Commands.getApplicableCommands(we);
        List<String> sections = Commands.getSections(applicableCommands);

        JMenu mnCommands = new JMenu("Tools");
        mnCommandsList.clear();
        for (String section : sections) {
            JMenu mnSection = mnCommands;
            if (!section.isEmpty()) {
                mnSection = new JMenu(section);
                if (isPromotedSection(section)) {
                    String menuName = getMenuNameFromSection(section);
                    mnSection.setText(menuName);
                    mnCommandsList.add(mnSection);
                } else {
                    mnCommands.add(mnSection);
                    mnCommandsList.addFirst(mnCommands);
                }
            }
            List<Command> sectionCommands = Commands.getSectionCommands(section, applicableCommands);
            List<List<Command>> sectionCommandsPartitions = new LinkedList<>();
            sectionCommandsPartitions.add(Commands.getUnpositionedCommands(sectionCommands));
            sectionCommandsPartitions.add(Commands.getPositionedCommands(sectionCommands, Position.TOP));
            sectionCommandsPartitions.add(Commands.getPositionedCommands(sectionCommands, Position.MIDDLE));
            sectionCommandsPartitions.add(Commands.getPositionedCommands(sectionCommands, Position.BOTTOM));
            boolean needSeparator = false;
            for (List<Command> sectionCommandsPartition : sectionCommandsPartitions) {
                boolean isFirstItem = true;
                for (Command command : sectionCommandsPartition) {
                    if (needSeparator && isFirstItem) {
                        mnSection.addSeparator();
                    }
                    needSeparator = true;
                    isFirstItem = false;
                    CommandAction commandAction = new CommandAction(command);
                    ActionMenuItem miCommand = new ActionMenuItem(commandAction);
                    miCommand.addScriptedActionListener(mainWindow.getDefaultActionListener());
                    mnSection.add(miCommand);
                }
            }
        }
        addCommandsMenu();
    }

    public static boolean isPromotedSection(String section) {
        return (section != null) && section.startsWith(MENU_SECTION_PROMOTED_PREFIX);
    }

    public static String getMenuNameFromSection(String section) {
        String result = "";
        if (section != null) {
            if (section.startsWith(MENU_SECTION_PROMOTED_PREFIX)) {
                result = section.substring(MENU_SECTION_PROMOTED_PREFIX.length());
            } else {
                result = section;
            }
        }
        return result.trim();
    }

    private void addCommandsMenu() {
        for (JMenu mnCommands : mnCommandsList) {
            add(mnCommands);
        }
        remove(mnHelp);
        add(mnHelp);
        revalidate();
    }

    public void removeCommandsMenu() {
        for (JMenu mnCommands : mnCommandsList) {
            remove(mnCommands);
        }
        revalidate();
    }

    public void updateCommandsMenuState(boolean enable) {
        for (JMenu mnCommands : mnCommandsList) {
            mnCommands.setEnabled(enable);
        }
    }

    public void setMenuForWorkspaceEntry(final WorkspaceEntry we) {
        we.updateActionState();
        createCommandsMenu(we);
        setExportMenu(we);
    }
}
