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
package org.workcraft.gui.trees;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.workcraft.gui.workspace.Path;

public class TreeWindow<Node> extends JPanel
{
	private JTree tree;
	private final TreePopupProvider<Node> popupProvider;

	public TreeWindow(TreeSource<Node> source, TreeDecorator<Node> decorator, TreePopupProvider<Node> popupProvider)
	{
		this.popupProvider = popupProvider;
		startup(source, decorator);
	}

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public Node selected()
	{
		return (Node)tree.getSelectionPath().getLastPathComponent();
	}

	public JTree getTreeComponent() {
		return tree;
	}

	public void startup(final TreeSource<Node> source, final TreeDecorator<Node> decorator)
	{
		tree = new JTree();
		tree.setFocusable(true);

		final TreeSourceAdapter<Node> sourceWithRestructuredTrapped = new TreeSourceAdapter<Node>(source)
		{
			public TreeListener<Node> getListener(final TreeListener<Node> chain) {
				return new TreeListenerAdapter<Node>(chain)
				{
					@Override
					public void restructured(Path<Node> path) {
						List<TreePath> expanded = new ArrayList<TreePath>();
						for(int i=0;i<tree.getRowCount();i++) {
							final TreePath treePath = tree.getPathForRow(i);
							if(tree.isExpanded(i))
								expanded.add(treePath);
						}

						super.restructured(path);

						for(TreePath p : expanded)
							tree.expandPath(p);
					}
				};
			};
		};

		final TreeModelWrapper<Node> modelWrapper = new TreeModelWrapper<Node>(sourceWithRestructuredTrapped);
		tree.setModel(modelWrapper);

		if (popupProvider!=null)
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
							popupProvider.getPopup(source.getRoot()).show(tree, x, y);
						} else
							popupProvider.getPopup(selected()).show(tree, x, y);
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


				String name = decorator.getName(node);
				boolean tricky = name.startsWith("!");
				if (tricky)
					name = name.substring(1);
				Component res = super.getTreeCellRendererComponent(tree, name, sel, expanded, leaf, row, hasFocus);

				if (tricky)
					res.setFont(res.getFont().deriveFont(Font.ITALIC));
				else
					res.setFont(res.getFont().deriveFont(Font.PLAIN));

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
				{
					sourceWithRestructuredTrapped.getListener().restructured(Path.root(source.getRoot()));
				}
			}
		}
		);
		setLayout(new BorderLayout(0,0));
		this.add(tree, BorderLayout.CENTER);
	}

	public void makeVisible(Path<Node> node) {
		 tree.makeVisible(new TreePath(Path.getPath(node).toArray()));
	}

	public static <Node> TreeWindow<Node> create(TreeSource<Node> source, TreeDecorator<Node> decorator, TreePopupProvider<Node> popupProvider) {
		return new TreeWindow<Node>(source, decorator, popupProvider);
	}

}
