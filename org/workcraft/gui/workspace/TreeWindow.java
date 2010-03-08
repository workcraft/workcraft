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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeWindow<Node> extends JPanel
{
	private JTree tree;

	public TreeWindow(TreeSource<Node> source, TreeDecorator<Node> decorator)
	{
		startup(source, decorator);
	}

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public Node selected()
	{
		return (Node)tree.getSelectionPath().getLastPathComponent();
	}

	@SuppressWarnings("unchecked")
	private Path<Node> selectedPath()
	{
		final Object[] path = tree.getSelectionPath().getPath();
		return Path.create((Node[])path);
	}

	public void startup(final TreeSource<Node> source, final TreeDecorator<Node> decorator)
	{
		tree = new JTree();
		tree.setFocusable(false);
		final TreeModelWrapper<Node> modelWrapper = new TreeModelWrapper<Node>(source);
		tree.setModel(modelWrapper);
		tree.addMouseListener(new MouseAdapter()
			{
				public void mousePressed(java.awt.event.MouseEvent e) { maybeShowPopup(e); };
				public void mouseReleased(java.awt.event.MouseEvent e) { maybeShowPopup(e); };

				private void maybeShowPopup(MouseEvent e) {
					if(e.isPopupTrigger())
					{
						final int x = e.getX();
						final int y = e.getY();
						tree.setSelectionPath(tree.getClosestPathForLocation(x, y));
						final Rectangle rowBounds = tree.getRowBounds(tree.getSelectionRows()[0]);
						if (y < rowBounds.getMinY() || y > rowBounds.getMaxY() ) {
							tree.setSelectionPath(null);
							decorator.getPopupMenu(source.getRoot()).show(tree, x, y);
						} else
							decorator.getPopupMenu(selected()).show(tree, x, y);
					}
				}
			}
		);

		tree.setCellRenderer(new DefaultTreeCellRenderer()
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {
				Node node = (Node)value;
				Component res = super.getTreeCellRendererComponent(tree, decorator.getName(node), sel, expanded, leaf, row, hasFocus);
				final Icon icon = decorator.getIcon(node);
				//if(icon!=null)
					setIcon(icon);
				return res;
			}
		}
		);

		tree.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_F5)
					modelWrapper.update(selectedPath());
			}
		}
		);
		setLayout(new BorderLayout(0,0));
		this.add(tree, BorderLayout.CENTER);
	}

	public static <Node> TreeWindow<Node> create(TreeSource<Node> source, TreeDecorator<Node> decorator) {
		return new TreeWindow<Node>(source, decorator);
	}
}
