package org.workcraft.gui.workspace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.workcraft.framework.Framework;
import org.workcraft.framework.Document;


class WorkspaceWindowPopupListener extends MouseAdapter {
	private Framework framework;

	public WorkspaceWindowPopupListener(Framework framework) {
		this.framework = framework;
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
			if (node.getUserObject()!=null && node.getUserObject() instanceof Document)
			{
				final Document we = (Document) node.getUserObject();
				// add WorkspaceEntry menu items
				//popup.addSeparator();

				JMenuItem miRemove = new JMenuItem("Remove");
				miRemove.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						framework.getWorkspace().remove(we);
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