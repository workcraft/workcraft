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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.workcraft.Framework;
import org.workcraft.PluginInfo;
import org.workcraft.Tool;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.util.ListMap;
import org.workcraft.util.Pair;
import org.workcraft.util.Tools;
import org.workcraft.workspace.FileHandler;
import org.workcraft.workspace.WorkspaceEntry;

class WorkspaceWindowPopupListener extends MouseAdapter {
	private Framework framework;
	private WorkspaceWindow wsWindow;
	private HashMap<JMenuItem, FileHandler> handlers;
	private HashMap<JMenuItem, Tool> tools;

	public WorkspaceWindowPopupListener(Framework framework, WorkspaceWindow wsWindow) {
		this.framework = framework;
		this.wsWindow = wsWindow;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		JTree tree = (JTree) e.getComponent();
		tree.setSelectionPath(tree
				.getClosestPathForLocation(e.getX(), e.getY()));

		maybeShowPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			JPopupMenu popup = new JPopupMenu();

			handlers = new HashMap<JMenuItem, FileHandler>();
			tools = new HashMap<JMenuItem, Tool>();

			JTree tree = (JTree) e.getComponent();
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			DefaultMutableTreeNode node = null;

			boolean showWorkspaceItems = true;

			if (path != null) {
				node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node.getUserObject() != null
						&& node.getUserObject() instanceof WorkspaceEntry) {
					showWorkspaceItems = false;
					final WorkspaceEntry we = (WorkspaceEntry) node.getUserObject();

					if (we.getFile() != null) {
						// add WorkspaceEntry menu items
						PluginInfo[] handlersInfo = framework.getPluginManager()
						.getPluginsImplementing(FileHandler.class.getName());

						for (PluginInfo info : handlersInfo)
							try {
								FileHandler handler = (FileHandler) framework
								.getPluginManager().getSingleton(info);

								if (!handler.accept(we.getFile()))
									continue;
								JMenuItem mi = new JMenuItem(info.getDisplayName());
								handlers.put(mi, handler);
								mi.addActionListener(new ActionListener() {

									public void actionPerformed(ActionEvent e) {
										handlers.get(e.getSource()).execute(we.getFile(), framework);
									}
								});
								popup.add(mi);
							} catch (PluginInstantiationException e1) {
								throw new RuntimeException (e1);
							}
					}

					if (we.getModel()!=null) {
						JLabel label = new JLabel (we.getModel().getDisplayName()+ (we.getModel().getTitle().isEmpty()?"" : ("\"" + we.getModel().getTitle() + "\"" )));
						popup.add(label);
						popup.addSeparator();

						JMenuItem miOpenView = new JMenuItem("Open in editor");
						miOpenView.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								framework.getMainWindow().createEditorWindow(we);
							}
						});

						JMenuItem miSave = new JMenuItem("Save");
						miSave.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								try {
									framework.getMainWindow().save(we);
								} catch (OperationCancelledException e1) {
								}
							}
						});

						JMenuItem miSaveAs = new JMenuItem("Save as...");
						miSaveAs.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								try {
									framework.getMainWindow().saveAs(we);
								} catch (OperationCancelledException e1) {
								}
							}
						});

						popup.add(miSave);
						popup.add(miSaveAs);
						popup.add(miOpenView);

						ListMap<String, Pair<String, Tool>> applicableTools = Tools.getTools(we.getModel(), framework);
						List<String> sections = Tools.getSections(applicableTools);

						if (!sections.isEmpty())
							popup.addSeparator();

						for (String section : sections) {
							JMenu m = new JMenu(section);

							for (Pair<String, Tool> tool : Tools.getSectionTools(section, applicableTools)) {
								JMenuItem mi = new JMenuItem(tool.getFirst());
								tools.put(mi, tool.getSecond());

								mi.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										tools.get(e.getSource()).run(we.getModel(), framework);
									}
								});

								m.add(mi);
							}

							popup.add(m);
						}
					}

					popup.addSeparator();

					JMenuItem miRemove = new JMenuItem("Remove from workspace");
					miRemove.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							framework.getWorkspace().remove(we);
						}
					});
					popup.add(miRemove);
				}
			}

			if (showWorkspaceItems)
				for (Component c : wsWindow.createMenu().getMenuComponents())
					popup.add(c);

			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}