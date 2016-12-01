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
import org.workcraft.Tool;
import org.workcraft.MenuOrdering.Position;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainMenu;
import org.workcraft.gui.trees.TreePopupProvider;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.util.Tools;
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
        final HashMap<JMenuItem, Tool> tools = new HashMap<>();

        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();

        final File file = workspace.getFile(path);

        if (file.isDirectory()) {
            popup.addSeparator();
            final JMenuItem miLink = new JMenuItem("Link external files or directories...");
            miLink.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    wsWindow.addToWorkspace(path);
                }
            });
            popup.add(miLink);
            final JMenuItem miCreateWork = new JMenuItem("Create work...");
            miCreateWork.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    try {
                        framework.getMainWindow().createWork(path);
                    } catch (OperationCancelledException e1) { }
                }
            });
            popup.add(miCreateWork);
            final JMenuItem miCreateFolder = new JMenuItem("Create folder...");
            miCreateFolder.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
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
                            JOptionPane
                                    .showMessageDialog(
                                            framework.getMainWindow(),
                                            "The directory could not be created. Please check that the name does not contain any special characters.");
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
            final WorkspaceEntry openFile = workspace.getOpenFile(path);
            if (openFile == null) {
                if (file.exists()) {
                    if (file.getName().endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                        final JMenuItem miOpen = new JMenuItem("Open");
                        miOpen.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                framework.getMainWindow().openWork(file);
                            }
                        });
                        popup.add(miOpen);
                    }

                    for (PluginInfo<? extends FileHandler> info : framework.getPluginManager().getPlugins(FileHandler.class)) {
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
                ModelEntry modelEntry = openFile.getModelEntry();
                if (modelEntry != null) {
                    final Model model = modelEntry.getModel();
                    JLabel label = new JLabel(model.getDisplayName() + " " + (model.getTitle().isEmpty() ? "" : ("'" + model.getTitle() + "'")));
                    popup.add(label);
                    popup.addSeparator();

                    JMenuItem miOpenView = new JMenuItem("Open editor");
                    miOpenView.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            framework.getMainWindow().createEditorWindow(openFile);
                        }
                    });

                    JMenuItem miSave = new JMenuItem("Save");
                    miSave.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                framework.getMainWindow().save(openFile);
                            } catch (OperationCancelledException e1) {
                            }
                        }
                    });

                    JMenuItem miSaveAs = new JMenuItem("Save as...");
                    miSaveAs.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                framework.getMainWindow().saveAs(openFile);
                            } catch (OperationCancelledException e1) {
                            }
                        }
                    });

                    popup.add(miSave);
                    popup.add(miSaveAs);
                    popup.add(miOpenView);

                    List<Tool> applicableTools = Tools.getApplicableTools(modelEntry);
                    List<String> sections = Tools.getSections(applicableTools);

                    if (!sections.isEmpty()) {
                        popup.addSeparator();
                    }
                    for (String section : sections) {
                        String sectionMenuName = MainMenu.getMenuNameFromSection(section);
                        JMenu sectionMenu = new JMenu(sectionMenuName);

                        List<Tool> sectionTools = Tools.getSectionTools(section, applicableTools);
                        List<List<Tool>> sectionToolsPartitions = new LinkedList<>();
                        sectionToolsPartitions.add(Tools.getUnpositionedTools(sectionTools));
                        sectionToolsPartitions.add(Tools.getPositionedTools(sectionTools, Position.TOP));
                        sectionToolsPartitions.add(Tools.getPositionedTools(sectionTools, Position.MIDDLE));
                        sectionToolsPartitions.add(Tools.getPositionedTools(sectionTools, Position.BOTTOM));
                        boolean needSeparator = false;
                        for (List<Tool> sectionToolsPartition: sectionToolsPartitions) {
                            boolean isFirstItem = true;
                            for (Tool tool : sectionToolsPartition) {
                                if (needSeparator && isFirstItem) {
                                    sectionMenu.addSeparator();
                                }
                                needSeparator = true;
                                isFirstItem = false;
                                JMenuItem item = new JMenuItem(tool.getDisplayName().trim());
                                tools.put(item, tool);
                                item.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        Tools.run(openFile, tools.get(e.getSource()));
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
