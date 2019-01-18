package org.workcraft.gui.workspace;

import org.workcraft.Framework;
import org.workcraft.MenuOrdering.Position;
import org.workcraft.PluginManager;
import org.workcraft.commands.Command;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainMenu;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.trees.TreePopupProvider;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.util.Commands;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class WorkspacePopupProvider implements TreePopupProvider<Path<String>> {

    private final WorkspaceWindow wsWindow;

    public WorkspacePopupProvider(WorkspaceWindow wsWindow) {
        this.wsWindow = wsWindow;
    }

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
            miLink.addActionListener(event -> wsWindow.addToWorkspace(path));
            popup.add(miLink);
            final JMenuItem miCreateWork = new JMenuItem("Create work...");
            miCreateWork.addActionListener(event -> {
                try {
                    framework.getMainWindow().createWork(path);
                } catch (OperationCancelledException e1) {
                }
            });
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
                } catch (OperationCancelledException e1) {
                }
            });
            popup.add(miCreateFolder);
        }

        if (WorkspaceTree.isLeaf(workspace, path)) {
            popup.addSeparator();
            final WorkspaceEntry we = workspace.getWork(path);
            if (we == null) {
                if (file.exists()) {
                    if (file.getName().endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                        final JMenuItem miOpen = new JMenuItem("Open");
                        miOpen.addActionListener(event -> mainWindow.openWork(file));
                        popup.add(miOpen);
                    }

                    final PluginManager pluginManager = framework.getPluginManager();
                    for (PluginInfo<? extends FileHandler> info : pluginManager.getFileHandlerPlugins()) {
                        FileHandler handler = info.getSingleton();

                        if (!handler.accept(file)) {
                            continue;
                        }
                        JMenuItem mi = new JMenuItem(handler.getDisplayName());
                        handlers.put(mi, handler);
                        mi.addActionListener(event -> handlers.get(event.getSource()).execute(file));
                        popup.add(mi);
                    }
                }
            } else {
                ModelEntry me = we.getModelEntry();
                if (me != null) {
                    final Model model = me.getModel();
                    String title = model.getTitle();
                    JLabel label = new JLabel(model.getDisplayName() + " " + (title.isEmpty() ? "" : ("'" + title + "'")));
                    popup.add(label);
                    popup.addSeparator();

                    JMenuItem miOpenView = new JMenuItem("Open editor");
                    miOpenView.addActionListener(event -> mainWindow.createEditorWindow(we));

                    JMenuItem miSave = new JMenuItem("Save");
                    miSave.addActionListener(event -> {
                        try {
                            mainWindow.saveWork(we);
                        } catch (OperationCancelledException e1) {
                        }
                    });

                    JMenuItem miSaveAs = new JMenuItem("Save as...");
                    miSaveAs.addActionListener(event -> {
                        try {
                            mainWindow.saveWorkAs(we);
                        } catch (OperationCancelledException e1) {
                        }
                    });

                    popup.add(miSave);
                    popup.add(miSaveAs);
                    popup.add(miOpenView);

                    List<Command> applicableVisibleCommands = Commands.getApplicableVisibleCommands(we);
                    List<String> sections = Commands.getSections(applicableVisibleCommands);

                    if (!sections.isEmpty()) {
                        popup.addSeparator();
                    }
                    for (String section: sections) {
                        String sectionMenuName = MainMenu.getMenuNameFromSection(section);
                        JMenu sectionMenu = new JMenu(sectionMenuName);

                        List<Command> sectionCommands = Commands.getSectionCommands(section, applicableVisibleCommands);
                        List<List<Command>> sectionCommandsPartitions = new LinkedList<>();
                        sectionCommandsPartitions.add(Commands.getUnpositionedCommands(sectionCommands));
                        for (Position position: Position.values()) {
                            sectionCommandsPartitions.add(Commands.getPositionedCommands(sectionCommands, position));
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
                                item.addActionListener(event -> Commands.run(we, commands.get(event.getSource())));
                                sectionMenu.add(item);
                            }
                        }
                        popup.add(sectionMenu);
                    }
                }
            }
            popup.addSeparator();

            JMenuItem miRemove = new JMenuItem("Delete");
            miRemove.addActionListener(event -> {
                try {
                    workspace.deleteEntry(path);
                } catch (OperationCancelledException e1) {
                }
            });
            popup.add(miRemove);
        }

        popup.addSeparator();
        if (path.isEmpty()) {
            for (Component c : wsWindow.createMenu().getMenuComponents()) {
                popup.add(c);
            }
        }
        return popup;
    }

}
