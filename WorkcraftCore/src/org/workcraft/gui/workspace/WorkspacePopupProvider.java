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

package org.workcraft.gui.workspace;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.workcraft.Framework;
import org.workcraft.Command;
import org.workcraft.MenuOrdering.Position;
import org.workcraft.PluginManager;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainMenu;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.trees.TreePopupProvider;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.util.Commands;
import org.workcraft.workspace.FileHandler;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceTree;

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
            miLink.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wsWindow.addToWorkspace(path);
                }
            });
            popup.add(miLink);
            final JMenuItem miCreateWork = new JMenuItem("Create work...");
            miCreateWork.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        framework.getMainWindow().createWork(path);
                    } catch (OperationCancelledException e1) { }
                }
            });
            popup.add(miCreateWork);
            final JMenuItem miCreateFolder = new JMenuItem("Create folder...");
            miCreateFolder.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        createFolder(path);
                    } catch (OperationCancelledException e1) { }
                }

                private void createFolder(Path<String> path) throws OperationCancelledException {
                    String name;
                    while (true) {
                        name = JOptionPane.showInputDialog("Please enter the name of the new folder:", "");
                        if (name == null) {
                            throw new OperationCancelledException();
                        }
                        File newDir = workspace.getFile(Path.append(path, name));
                        if (!newDir.mkdir()) {
                            JOptionPane.showMessageDialog(mainWindow,
                                    "The directory could not be created.\n"
                                    + "Please check that the name does not contain any special characters.");
                        } else {
                            break;
                        }
                    }

                    workspace.fireWorkspaceChanged();
                }
            });
            popup.add(miCreateFolder);
        }

        if (WorkspaceTree.isLeaf(workspace, path)) {
            popup.addSeparator();
            final WorkspaceEntry we = workspace.getOpenFile(path);
            if (we == null) {
                if (file.exists()) {
                    if (file.getName().endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                        final JMenuItem miOpen = new JMenuItem("Open");
                        miOpen.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                mainWindow.openWork(file);
                            }
                        });
                        popup.add(miOpen);
                    }

                    final PluginManager pluginManager = framework.getPluginManager();
                    for (PluginInfo<? extends FileHandler> info : pluginManager.getPlugins(FileHandler.class)) {
                        FileHandler handler = info.getSingleton();

                        if (!handler.accept(file)) {
                            continue;
                        }
                        JMenuItem mi = new JMenuItem(handler.getDisplayName());
                        handlers.put(mi, handler);
                        mi.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                handlers.get(e.getSource()).execute(file);
                            }
                        });
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
                    miOpenView.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            mainWindow.createEditorWindow(we);
                        }
                    });

                    JMenuItem miSave = new JMenuItem("Save");
                    miSave.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                mainWindow.save(we);
                            } catch (OperationCancelledException e1) {
                            }
                        }
                    });

                    JMenuItem miSaveAs = new JMenuItem("Save as...");
                    miSaveAs.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                mainWindow.saveAs(we);
                            } catch (OperationCancelledException e1) {
                            }
                        }
                    });

                    popup.add(miSave);
                    popup.add(miSaveAs);
                    popup.add(miOpenView);

                    List<Command> applicableCommands = Commands.getApplicableCommands(we);
                    List<String> sections = Commands.getSections(applicableCommands);

                    if (!sections.isEmpty()) {
                        popup.addSeparator();
                    }
                    for (String section : sections) {
                        String sectionMenuName = MainMenu.getMenuNameFromSection(section);
                        JMenu sectionMenu = new JMenu(sectionMenuName);

                        List<Command> sectionCommands = Commands.getSectionCommands(section, applicableCommands);
                        List<List<Command>> sectionCommandsPartitions = new LinkedList<>();
                        sectionCommandsPartitions.add(Commands.getUnpositionedCommands(sectionCommands));
                        sectionCommandsPartitions.add(Commands.getPositionedCommands(sectionCommands, Position.TOP));
                        sectionCommandsPartitions.add(Commands.getPositionedCommands(sectionCommands, Position.MIDDLE));
                        sectionCommandsPartitions.add(Commands.getPositionedCommands(sectionCommands, Position.BOTTOM));
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
                                item.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        Commands.run(we, commands.get(e.getSource()));
                                    }
                                });
                                sectionMenu.add(item);
                            }
                        }
                        popup.add(sectionMenu);
                    }
                }
            }
            popup.addSeparator();

            JMenuItem miRemove = new JMenuItem("Delete");
            miRemove.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        workspace.delete(path);
                    } catch (OperationCancelledException e1) { }
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
