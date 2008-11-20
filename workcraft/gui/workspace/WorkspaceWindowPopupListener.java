package org.workcraft.gui.workspace;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.workcraft.dom.DisplayName;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.framework.workspace.FileHandler;
import org.workcraft.framework.workspace.WorkspaceEntry;

class WorkspaceWindowPopupListener extends MouseAdapter {
	private Framework framework;
	private WorkspaceWindow wsWindow;
	private HashMap<JMenuItem, FileHandler> handlers;
	private HashMap<JMenuItem, PluginInfo> models;

	public WorkspaceWindowPopupListener(Framework framework, WorkspaceWindow wsWindow) {
		this.framework = framework;
		this.wsWindow = wsWindow;
		handlers = new HashMap<JMenuItem, FileHandler>();
	}

	public void mousePressed(MouseEvent e) {
		JTree tree = (JTree) e.getComponent();
		tree.setSelectionPath(tree
				.getClosestPathForLocation(e.getX(), e.getY()));

		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			JPopupMenu popup = new JPopupMenu();

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

					// add WorkspaceEntry menu items
					PluginInfo[] handlersInfo = framework.getPluginManager()
					.getPlugins(FileHandler.class);

					for (PluginInfo info : handlersInfo) {
						try {
							FileHandler handler = (FileHandler) framework
							.getPluginManager().getSingleton(info);
							if (!handler.accept(we.getFile()))
								continue;
							JMenuItem mi = new JMenuItem(info.getDisplayName());
							handlers.put(mi, handler);
							mi.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									handlers.get(e.getSource()).execute(we.getFile());
								}
							});
							popup.add(mi);
						} catch (PluginInstantiationException e1) {
							System.err.println(e1.getMessage());
						}
					}

					JMenuItem miRemove = new JMenuItem("Remove");
					miRemove.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							framework.getWorkspace().remove(we);
						}
					});
					popup.add(miRemove);
				}
			}

			if (showWorkspaceItems) {
				for (Component c : wsWindow.createMenu().getMenuComponents())
				popup.add(c);
			}





			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}