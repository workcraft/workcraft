package org.workcraft.gui.workspace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.workcraft.dom.DisplayName;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.framework.workspace.FileHandler;


class WorkspaceWindowPopupListener extends MouseAdapter {
	private Framework framework;
	private HashMap<JMenuItem, FileHandler> handlers;

	public WorkspaceWindowPopupListener(Framework framework) {
		this.framework = framework;
		handlers = new HashMap<JMenuItem, FileHandler>();
	}

	public void mousePressed(MouseEvent e) {
		JTree tree = (JTree)e.getComponent();
		tree.setSelectionPath(tree.getClosestPathForLocation(e.getX(), e.getY()));

		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			JTree tree = (JTree)e.getComponent();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getClosestPathForLocation(e.getX(), e.getY()).getLastPathComponent();

			JPopupMenu popup = new JPopupMenu();
			if (node.getUserObject()!=null && node.getUserObject() instanceof WorkspaceEntry)
			{
				final WorkspaceEntry we = (WorkspaceEntry) node.getUserObject();
				// add WorkspaceEntry menu items

				PluginInfo[] handlersInfo = framework.getPluginManager().getPluginInfo("org.workcraft.framework.workspace.FileHandler");

				for (PluginInfo info :handlersInfo) {
					try {
						FileHandler handler = (FileHandler)framework.getPluginManager().getSingleton(info);
						if (!handler.accept(we.file()))
							continue;
						DisplayName name = handler.getClass().getAnnotation(DisplayName.class);
						JMenuItem mi = new JMenuItem ( (name==null)?handler.getClass().getSimpleName():name.value() );
						handlers.put(mi, handler);
						mi.addActionListener( new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								handlers.get(e.getSource()).execute(we.file());
							}
						});
						popup.add(mi);
					} catch (PluginInstantiationException e1) {
						System.err.println (e1.getMessage());
					}
				}

				popup.addSeparator();

				JMenuItem miRemove = new JMenuItem("Remove");
				miRemove.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						framework.getWorkspace().remove(we.file());
					}
				});
				popup.add(miRemove);
			}

			JMenuItem miAdd = new JMenuItem("Add items...");
			miAdd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					framework.getMainFrame().addToWorkspace();
				}
			});

			popup.add(miAdd);

			popup.show(e.getComponent(),
					e.getX(), e.getY());
		}
	}
}