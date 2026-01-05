package org.workcraft.gui.workspace;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Menu;
import org.workcraft.gui.panels.WorkspacePanel;
import org.workcraft.gui.trees.TreePopupProvider;
import org.workcraft.plugins.PluginManager;
import org.workcraft.utils.CommandUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class WorkspacePopupProvider implements TreePopupProvider<Path<String>> {

    private final WorkspacePanel workspacePanel;
    private boolean allowFileRemoval = false;

    public WorkspacePopupProvider(WorkspacePanel workspacePanel) {
        this.workspacePanel = workspacePanel;
    }

    @Override
    public JPopupMenu getPopup(final Path<String> path) {
        JPopupMenu popup = new JPopupMenu();

        Workspace workspace = Framework.getInstance().getWorkspace();
        File file = workspace.getFile(path);
        if (file.isDirectory()) {
            addDirectoryMenuItems(path, popup);
        }

        if (WorkspaceTree.isLeaf(workspace, path)) {
            popup.addSeparator();
            WorkspaceEntry we = workspace.getWork(path);
            if (we == null) {
                addFileMenuItems(file, popup);
            } else {
                addWorkMenuItems(we, popup);
                addCommandMenus(we, popup);
            }

            if (allowFileRemoval) {
                popup.addSeparator();
                JMenuItem miRemove = new JMenuItem("Delete");
                miRemove.addActionListener(event -> workspace.deleteEntry(path));
                popup.add(miRemove);
            }
        }

        if (path.isEmpty()) {
            popup.addSeparator();
            for (Component component : workspacePanel.createMenu().getMenuComponents()) {
                popup.add(component);
            }
        }
        return popup;
    }

    private void addDirectoryMenuItems(Path<String> path, JPopupMenu popup) {
        Framework framework = Framework.getInstance();
        Workspace workspace = framework.getWorkspace();
        MainWindow mainWindow = framework.getMainWindow();

        popup.addSeparator();
        final JMenuItem miLink = new JMenuItem("Link external files or directories...");
        miLink.addActionListener(event -> workspacePanel.addToWorkspace(path));
        popup.add(miLink);
        final JMenuItem miCreateWork = new JMenuItem("Create work...");
        miCreateWork.addActionListener(event -> mainWindow.createWork(path));
        popup.add(miCreateWork);
        final JMenuItem miCreateFolder = new JMenuItem("Create folder...");

        miCreateFolder.addActionListener(event -> {
            try {
                String name;
                while (true) {
                    name = DialogUtils.showInput("Please enter the name of the new folder:", "");
                    if (name == null) {
                        throw new OperationCancelledException();
                    }
                    File newDir = workspace.getFile(Path.append(path, name));
                    if (!newDir.mkdir()) {
                        DialogUtils.showWarning("The directory could not be created.\n"
                                + "Please check that the name does not contain any special characters.");
                    } else {
                        break;
                    }
                }
                workspace.fireWorkspaceChanged();
            } catch (OperationCancelledException ignored) {
            }
        });
        popup.add(miCreateFolder);
    }

    private static void addFileMenuItems(File file, JPopupMenu popup) {
        if (file.exists()) {
            Framework framework = Framework.getInstance();
            MainWindow mainWindow = framework.getMainWindow();
            if (FileFilters.isWorkFile(file)) {
                final JMenuItem miOpen = new JMenuItem("Open");
                miOpen.addActionListener(event -> mainWindow.openWork(file));
                popup.add(miOpen);
            }

            HashMap<JMenuItem, FileHandler> handlers = new HashMap<>();
            PluginManager pm = framework.getPluginManager();
            for (FileHandler fileHandler : pm.getSortedFileHandlers()) {
                if (fileHandler.accept(file)) {
                    JMenuItem mi = new JMenuItem(fileHandler.getDisplayName());
                    handlers.put(mi, fileHandler);
                    mi.addActionListener(event -> handlers.get(event.getSource()).execute(file));
                    popup.add(mi);
                }
            }
        }
    }

    private static void addWorkMenuItems(WorkspaceEntry we, JPopupMenu popup) {
        ModelEntry me = we.getModelEntry();
        String displayName = me.getModel().getDisplayName();
        String title = me.getModel().getTitle();
        JLabel label = new JLabel(displayName + ' ' + (title.isEmpty() ? "" : ("'" + title + "'")));
        popup.add(label);
        popup.addSeparator();

        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        JMenuItem miOpenEditor = new JMenuItem("Open editor");
        miOpenEditor.addActionListener(event -> mainWindow.getOrCreateEditor(we));
        popup.add(miOpenEditor);

        JMenuItem miSave = new JMenuItem("Save");
        miSave.addActionListener(event -> mainWindow.saveWork(we));
        popup.add(miSave);

        JMenuItem miSaveAs = new JMenuItem("Save as...");
        miSaveAs.addActionListener(event -> mainWindow.saveWorkAs(we));
        popup.add(miSaveAs);

        JMenuItem miClose = new JMenuItem("Close");
        miClose.addActionListener(event -> mainWindow.closeEditor(we));
        popup.add(miClose);

        JMenu mnExport = new JMenu("Export");
        mnExport.setEnabled(false);
        Menu.addExporters(mnExport, we);
        popup.add(mnExport);
    }

    private static void addCommandMenus(WorkspaceEntry we, JPopupMenu popup) {
        List<Command> applicableVisibleCommands = CommandUtils.getApplicableVisibleCommands(we);
        List<String> orderedCategoryNames = CommandUtils.getOrderedCategoryNames(applicableVisibleCommands);
        if (!orderedCategoryNames.isEmpty()) {
            popup.addSeparator();
        }
        for (String categoryName : orderedCategoryNames) {
            JMenu menu = CommandUtils.createCommandsMenu(categoryName, applicableVisibleCommands);
            popup.add(menu);
        }
    }

    public void setAllowFileRemoval(boolean value) {
        allowFileRemoval = value;
    }

}
