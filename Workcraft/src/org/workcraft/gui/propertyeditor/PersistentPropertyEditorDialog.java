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

package org.workcraft.gui.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.PluginInfo;

public class PersistentPropertyEditorDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel propertiesPane;
	private JPanel buttonsPane;

	private JButton okButton;
	private JScrollPane sectionScroll;

	private DefaultMutableTreeNode sectionRoot;

		private JTree sectionTree;

	private final Framework framework;
	private final PropertyEditorTable propertiesTable;

	public PersistentPropertyEditorDialog(MainWindow owner) {
		super(owner);

		framework = owner.getFramework();
		propertiesTable = new PropertyEditorTable(framework);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setModal(true);
		setTitle("Settings");
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				ok();
			}
		});

		Dimension parentSize = owner.getSize();
		this.setSize(parentSize.width / 2, parentSize.height / 2);
		Dimension mySize = getSize();
		owner.getLocationOnScreen();

		this.setLocation (((parentSize.width - mySize.width)/2) + 0, ((parentSize.height - mySize.height)/2) + 0);

		initComponents();

		loadSections();
	}

	public DefaultMutableTreeNode getSectionNode (DefaultMutableTreeNode node, String section) {
		int dotPos = section.indexOf('.');

		String thisLevel, nextLevel;

		if (dotPos < 0) {
			thisLevel = section;
			nextLevel = null;
		}
		else {
			thisLevel = section.substring(0, dotPos);
			nextLevel = section.substring(dotPos+1);
		}

		DefaultMutableTreeNode thisLevelNode = null;

		for (int i=0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			if (! (child.getUserObject() instanceof String))
				continue;

			if (((String)child.getUserObject()).equals(thisLevel)) {
				thisLevelNode = child;
				break;
			}
		}

		if (thisLevelNode == null)
			thisLevelNode = new DefaultMutableTreeNode(thisLevel);

		node.add(thisLevelNode);

		if (nextLevel == null)
			return thisLevelNode;
		else
			return getSectionNode(thisLevelNode, nextLevel);
	}

	private void addItem (String section, PluginInfo<? extends PersistentPropertyEditable> info) {
		DefaultMutableTreeNode sectionNode = getSectionNode(sectionRoot, section);
		sectionNode.add(new DefaultMutableTreeNode(info));
	}

	private void loadSections() {
		for (PluginInfo<? extends PersistentPropertyEditable> info : framework.getPluginManager().getPlugins(PersistentPropertyEditable.class)) {
			PersistentPropertyEditable e = info.getSingleton();
			addItem (e.getSection(), info);
		}

		sectionTree.setModel(new DefaultTreeModel(sectionRoot));
	}

	private void setObject(PluginInfo<? extends PersistentPropertyEditable> info) {
		if (info == null)
			propertiesTable.setObject(null);
		else {
			propertiesTable.setObject(info.getSingleton());
		}
	}

	private void initComponents() {
		contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);

		sectionScroll = new JScrollPane();

		sectionRoot = new DefaultMutableTreeNode("root");

		sectionTree = new JTree();
		sectionTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		sectionTree.setRootVisible(false);
		sectionTree.setShowsRootHandles(true);

		sectionTree.addTreeSelectionListener(new TreeSelectionListener() {
			@SuppressWarnings("unchecked")
			public void valueChanged(TreeSelectionEvent e) {
				Object userObject = ((DefaultMutableTreeNode)e.getPath().getLastPathComponent()).getUserObject();
				if (userObject instanceof PluginInfo) {
					setObject((PluginInfo<? extends PersistentPropertyEditable>) userObject );
				} else {
					setObject(null);
				}
			}
		});

		sectionScroll.setViewportView(sectionTree);
		sectionScroll.setMinimumSize(new Dimension (200,0));
		sectionScroll.setPreferredSize(new Dimension (200,0));
		sectionScroll.setBorder(BorderFactory.createTitledBorder("Section"));

		propertiesPane = new JPanel();
		propertiesPane.setBorder(BorderFactory.createTitledBorder("Selection properties"));
		propertiesPane.setLayout(new BorderLayout());
		propertiesPane.add(propertiesTable);

		buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

		okButton = new JButton();
		okButton.setPreferredSize(new Dimension(100, 20));
		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
					ok();
			}
		});

		buttonsPane.add(okButton);

		contentPane.add(sectionScroll, BorderLayout.WEST);
		contentPane.add(propertiesPane, BorderLayout.CENTER);
		contentPane.add(buttonsPane, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(okButton);
	}


	private void ok() {
		setObject(null);
		setVisible(false);
	}
}
