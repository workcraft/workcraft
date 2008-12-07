package org.workcraft.gui.workspace;

import java.awt.BorderLayout;
import java.io.File;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.workspace.WorkspaceEntry;
import org.workcraft.framework.workspace.WorkspaceListener;


@SuppressWarnings("serial")
public class WorkspaceWindow extends JPanel implements WorkspaceListener {
	private JScrollPane scrollPane = null;
	private JTree workspaceTree = null;

	private Framework framework;

	private DefaultMutableTreeNode workspaceRoot;
	private HashMap<String, DefaultMutableTreeNode> folderNodes = new HashMap<String, DefaultMutableTreeNode>();
	private HashMap<String, DefaultMutableTreeNode> modelTypeNodes = new HashMap<String, DefaultMutableTreeNode>();
	private HashMap<WorkspaceEntry, DefaultMutableTreeNode> entryNodes = new HashMap<WorkspaceEntry, DefaultMutableTreeNode>();

	public WorkspaceWindow(Framework framework) {
		super();
		this.framework = framework;
	}

	public void startup() {
		scrollPane = new JScrollPane();

		workspaceRoot = new DefaultMutableTreeNode("[new workspace]");

		workspaceTree = new JTree();
		workspaceTree.setModel(new DefaultTreeModel(workspaceRoot));
		workspaceTree.addMouseListener(new WorkspaceWindowPopupListener(framework, this));

		scrollPane.setViewportView(workspaceTree);

		this.setLayout( new BorderLayout(0,0));
		this.add(scrollPane, BorderLayout.CENTER);
	}

	public void shutdown() {
	}

	protected int getInsertPoint (DefaultMutableTreeNode node, String caption) {
		if (node.getChildCount() == 0)
			return 0;

		int i;

		for (i=0; i<node.getChildCount(); i++) {
			if (node.getChildAt(i).toString().compareToIgnoreCase(caption) > 0)
				return i;
		}

		return i;

	}

	@Override
	public void entryAdded(WorkspaceEntry we) {
		DefaultMutableTreeNode node;

		if(!entryNodes.containsKey(we)) {
			node = new DefaultMutableTreeNode();
			node.setUserObject(we);
			entryNodes.put(we, node);

			String folderName = "work";

			if (we.getFile() != null) {
				String s = we.getFile().getName();
				int i = s.lastIndexOf('.');

				if (i > 0 &&  i < s.length() - 1) {
					folderName = s.substring(i+1).toLowerCase();
				}
			}

			DefaultTreeModel treeModel = (DefaultTreeModel)workspaceTree.getModel();
			DefaultMutableTreeNode folderNode = folderNodes.get(folderName);

			if(folderNode==null) {
				folderNode = new DefaultMutableTreeNode(folderName) ;
				treeModel.insertNodeInto(folderNode, workspaceRoot, getInsertPoint(workspaceRoot, folderName));
				//workspaceRoot.add(folderNode);
				folderNodes.put(folderName, folderNode);
			}

			String folder = we.getModel().getDisplayName();

			if (folder != null) {
				DefaultMutableTreeNode modelTypeNode = modelTypeNodes.get(folder);
				if (modelTypeNode == null) {
					modelTypeNode = new DefaultMutableTreeNode(folder);
					treeModel.insertNodeInto(modelTypeNode, folderNode, getInsertPoint(folderNode, folder));
					//folderNode.add(modelTypeNode);
					modelTypeNodes.put(folder, modelTypeNode);
				}
				treeModel.insertNodeInto(node, modelTypeNode, getInsertPoint(modelTypeNode, node.toString()));
				//modelTypeNode.add(node);
			}
			else
				treeModel.insertNodeInto(node, folderNode, getInsertPoint(folderNode, node.toString()));
			//folderNode.add(node);
			workspaceTree.makeVisible(new TreePath(node.getPath()));
		}
	}

	@Override
	public void entryRemoved(WorkspaceEntry we) {
		if (entryNodes.containsKey(we)) {
			DefaultTreeModel treeModel = (DefaultTreeModel)workspaceTree.getModel();
			DefaultMutableTreeNode node = entryNodes.get(we);
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
			DefaultMutableTreeNode parent2 = (DefaultMutableTreeNode)parent.getParent();
			treeModel.removeNodeFromParent(node);

			if (parent.getChildCount() == 0) {
				treeModel.removeNodeFromParent(parent);
				if (parent2 != workspaceRoot)
					modelTypeNodes.remove(parent.toString());
				else
					folderNodes.remove(parent.toString());
			}
			if (parent2 != workspaceRoot && parent2.getChildCount() == 0) {
				treeModel.removeNodeFromParent(parent2);
				folderNodes.remove(parent2.toString());
			}

			entryNodes.remove(we);
		}

	}

	@Override
	public void workspaceSaved() {
		String title = framework.getWorkspace().getFilePath();
		if (title.isEmpty())
			title = "new workspace";
		title = "(" + title + ")";
		if (framework.getWorkspace().isChanged())
			title = "*" + title;

		workspaceRoot.setUserObject(title);
	}

	public JMenu createMenu() {
		JMenu menu = new JMenu("Workspace");


		JMenuItem miNewModel = new JMenuItem("Create new model...");
		miNewModel.addActionListener(framework.getMainWindow().getDefaultActionListener());
		miNewModel.setActionCommand("gui.createWork()");

		JMenuItem miAdd = new JMenuItem("Add files to workspace...");
		miAdd.addActionListener(framework.getMainWindow().getDefaultActionListener());
		miAdd.setActionCommand("gui.getWorkspaceView().addToWorkspace()");

		JMenuItem miSave = new JMenuItem("Save workspace");
		miSave.addActionListener(framework.getMainWindow().getDefaultActionListener());
		miSave.setActionCommand("gui.getWorkspaceView().saveWorkspace()");

		JMenuItem miSaveAs = new JMenuItem("Save workspace as...");
		miSaveAs.addActionListener(framework.getMainWindow().getDefaultActionListener());
		miSaveAs.setActionCommand("gui.getWorkspaceView().saveWorkspaceAs()");

		menu.add(miNewModel);
		menu.addSeparator();
		menu.add(miAdd);
		menu.add(miSave);
		menu.add(miSaveAs);

		return menu;
	}

	public void addToWorkspace() {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.addChoosableFileFilter(FileFilters.DOCUMENT_FILES);
		fc.setFileFilter(fc.getAcceptAllFileFilter());
		if(fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
			for(File file : fc.getSelectedFiles()) {
				try {
					framework.getWorkspace().add(file.getPath());
				} catch (ModelLoadFailedException e) {
					JOptionPane.showMessageDialog(null, "The .work file could not be loaded. The file may be corrupted or in wrong format.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				} catch (VisualModelConstructionException e) {
					JOptionPane.showMessageDialog(null, "The .work file could not be loaded. The file may be corrupted or in wrong format.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	public void saveWorkspaceAs() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(FileFilters.WORKSPACE_FILES);
		if(fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
			framework.getWorkspace().save(FileFilters.checkSaveExtension(fc.getSelectedFile().getPath(), FileFilters.WORKSPACE_EXTENSION));
		}
	}

	@Override
	public void modelLoaded(WorkspaceEntry we) {
		// TODO Auto-generated method stub

	}
}
