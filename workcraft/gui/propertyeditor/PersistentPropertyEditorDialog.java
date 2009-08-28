package org.workcraft.gui.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

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

import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.gui.MainWindow;

public class PersistentPropertyEditorDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel propertiesPane;
	private JPanel buttonsPane;

	private JButton okButton;
	private JScrollPane sectionScroll;

	private DefaultMutableTreeNode sectionRoot;

		private JTree sectionTree;

	private PropertyEditorTable propertiesTable = new PropertyEditorTable();

	private Framework framework;


	public PersistentPropertyEditorDialog(MainWindow owner) {
		super(owner);

		framework = owner.getFramework();

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setModal(true);
		setTitle("Settings");

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

	private void addItem (String section, PluginInfo info) {
		DefaultMutableTreeNode sectionNode = getSectionNode(sectionRoot, section);
		sectionNode.add(new DefaultMutableTreeNode(info));
	}

	private void loadSections() {
		PluginInfo[] infos = framework.getPluginManager().getPlugins(PersistentPropertyEditable.class.getName());
		for (PluginInfo info : infos) {
			try {
				PersistentPropertyEditable e = (PersistentPropertyEditable)framework.getPluginManager().getSingleton(info);
				addItem (e.getSection(), info);
			} catch (PluginInstantiationException e) {
				e.printStackTrace();
			}

		}

		sectionTree.setModel(new DefaultTreeModel(sectionRoot));
	}

	private void setObject(PluginInfo info) {
		if (info == null)
			propertiesTable.setObject(null);
		else {
			try {
				PersistentPropertyEditable e = (PersistentPropertyEditable) framework.getPluginManager().getSingleton(info);
				propertiesTable.setObject(e);
			} catch (PluginInstantiationException e) {
				e.printStackTrace();
			}
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
			public void valueChanged(TreeSelectionEvent e) {
				Object userObject = ((DefaultMutableTreeNode)e.getPath().getLastPathComponent()).getUserObject();
				if (userObject instanceof PluginInfo) {
					setObject( (PluginInfo) userObject );
				} else {
					setObject (null);
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
	}


	private void ok() {
		setObject(null);
		setVisible(false);
	}
}
