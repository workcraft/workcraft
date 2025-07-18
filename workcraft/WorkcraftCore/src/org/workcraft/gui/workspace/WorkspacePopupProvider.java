package org.workcraft.gui.workspace;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.commands.MenuOrdering.Position;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Menu;
import org.workcraft.gui.trees.TreePopupProvider;
import org.workcraft.plugins.PluginManager;
import org.workcraft.utils.CommandUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class WorkspacePopupProvider implements TreePopupProvider<Path<String>> {

    private final WorkspaceWindow workspaceWindow;

    private boolean allowFileRemoval = false;

    public WorkspacePopupProvider(WorkspaceWindow workspaceWindow) {
        this.workspaceWindow = workspaceWindow;
    }

    @Override
    public JPopupMenu getPopup(final Path<String> path) {
        JPopupMenu popup = new JPopupMenu();

        final HashMap<JMenuItem, FileHandler> handlers = new HashMap<>();
        final HashMap<JMenuItem, Command> commands = new HashMap<>();

        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        final MainWindow mainWindow = framework.getMainWindow();

        final File file = workspace.getFile(path);

        if (file.isDirectory()) {
            popup.addSeparator();
            final JMenuItem miLink = new JMenuItem("Link external files or directories...");
            miLink.addActionListener(event -> workspaceWindow.addToWorkspace(path));
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

        if (WorkspaceTree.isLeaf(workspace, path)) {
            popup.addSeparator();
            final WorkspaceEntry we = workspace.getWork(path);
            if (we == null) {
                if (file.exists()) {
                    if (FileFilters.isWorkFile(file)) {
                        final JMenuItem miOpen = new JMenuItem("Open");
                        miOpen.addActionListener(event -> mainWindow.openWork(file));
                        popup.add(miOpen);
                    }

                    final PluginManager pm = framework.getPluginManager();
                    for (FileHandler fileHandler : pm.getSortedFileHandlers()) {
                        if (fileHandler.accept(file)) {
                            JMenuItem mi = new JMenuItem(fileHandler.getDisplayName());
                            handlers.put(mi, fileHandler);
                            mi.addActionListener(event -> handlers.get(event.getSource()).execute(file));
                            popup.add(mi);
                        }
                    }
                }
            } else {
                ModelEntry me = we.getModelEntry();
                if (me != null) {
                    final Model<?, ?> model = me.getModel();
                    String title = model.getTitle();
                    JLabel label = new JLabel(model.getDisplayName() + ' ' + (title.isEmpty() ? "" : ("'" + title + "'")));
                    popup.add(label);
                    popup.addSeparator();

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

                    List<Command> applicableVisibleCommands = CommandUtils.getApplicableVisibleCommands(we);
                    List<String> sections = CommandUtils.getSections(applicableVisibleCommands);

                    if (!sections.isEmpty()) {
                        popup.addSeparator();
                    }
                    for (String section: sections) {
                        String sectionMenuName = Menu.getMenuNameFromSection(section);
                        JMenu sectionMenu = new JMenu(sectionMenuName);

                        List<Command> sectionCommands = CommandUtils.getSectionCommands(section, applicableVisibleCommands);
                        List<List<Command>> sectionCommandsPartitions = new LinkedList<>();
                        sectionCommandsPartitions.add(CommandUtils.getUnpositionedCommands(sectionCommands));
                        for (Position position: Position.values()) {
                            sectionCommandsPartitions.add(CommandUtils.getPositionedCommands(sectionCommands, position));
                        }
                        boolean needSeparator = false;
                        for (List<Command> sectionCommandsPartition: sectionCommandsPartitions) {
                            boolean isFirstItem = true;
                            for (Command command : sectionCommandsPartition) {
                                if (needSeparator && isFirstItem) {
                                    sectionMenu.addSeparator();
                                }
                                needSeparator = true;
                                isFirstItem = false;
                                JMenuItem item = new JMenuItem(command.getDisplayName().trim());
                                commands.put(item, command);
                                item.addActionListener(event -> commands.get(event.getSource()).run(we));
                                sectionMenu.add(item);
                            }
                        }
                        popup.add(sectionMenu);
                    }
                }
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
            for (Component c : workspaceWindow.createMenu().getMenuComponents()) {
                popup.add(c);
            }
        }
        return popup;
    }

    public void setAllowFileRemoval(boolean value) {
        allowFileRemoval = value;
    }

}
