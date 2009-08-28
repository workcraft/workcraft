package org.workcraft.gui.workspace;

import java.awt.BorderLayout;
import java.io.File;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.exceptions.OperationCancelledException;
import org.workcraft.framework.workspace.WorkspaceEntry;
import org.workcraft.framework.workspace.WorkspaceListener;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionMenuItem;


@SuppressWarnings("serial")
public class WorkspaceWindow extends JPanel implements WorkspaceListener {
	public static class Actions {
		public static final ScriptedAction ADD_FILES_TO_WORKSPACE_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.getWorkspaceView().addToWorkspace()";
			}
			public String getText() {
				return "Add files to workspace...";
			};
		};

		public static final ScriptedAction OPEN_WORKSPACE_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.getWorkspaceView().openWorkspace()";
			}
			public String getText() {
				return "Open workspace...";
			};
		};

		public static final ScriptedAction SAVE_WORKSPACE_ACTION = new ScriptedAction() {
			public String getScript() {
				return MainWindow.Actions.tryOperation("mainWindow.getWorkspaceView().saveWorkspace()");
			}
			public String getText() {
				return "Save workspace";
			};
		};
		public static final ScriptedAction SAVE_WORKSPACE_AS_ACTION = new ScriptedAction() {
			public String getScript() {
				return MainWindow.Actions.tryOperation("mainWindow.getWorkspaceView().saveWorkspaceAs()");
			}
			public String getText() {
				return "Save workspace as...";
			};
		};
		public static final ScriptedAction NEW_WORKSPACE_AS_ACTION = new ScriptedAction() {
			public String getScript() {
				return MainWindow.Actions.tryOperation("mainWindow.getWorkspaceView().newWorkspace()");
			}
			public String getText() {
				return "New workspace";
			};
		};
	}

	private JScrollPane scrollPane = null;
	private JTree workspaceTree = null;

	private MainWindow mainWindow;
	private Framework framework;
	private String lastSavePath = null;
	private String lastOpenPath = null;

	private DefaultMutableTreeNode workspaceRoot;
	private HashMap<String, DefaultMutableTreeNode> folderNodes = new HashMap<String, DefaultMutableTreeNode>();
	private HashMap<String, DefaultMutableTreeNode> modelTypeNodes = new HashMap<String, DefaultMutableTreeNode>();
	private HashMap<WorkspaceEntry, DefaultMutableTreeNode> entryNodes = new HashMap<WorkspaceEntry, DefaultMutableTreeNode>();

	public WorkspaceWindow(MainWindow mainWindow) {
		super();
		this.mainWindow = mainWindow;
		this.framework = mainWindow.getFramework();
	}

	public void startup() {
		scrollPane = new JScrollPane();

		workspaceRoot = new DefaultMutableTreeNode("(default workspace)");

		workspaceTree = new JTree();
		workspaceTree.setModel(new DefaultTreeModel(workspaceRoot));
		workspaceTree.addMouseListener(new WorkspaceWindowPopupListener(framework, this));

		scrollPane.setViewportView(workspaceTree);

		setLayout( new BorderLayout(0,0));
		this.add(scrollPane, BorderLayout.CENTER);

		lastSavePath = framework.getConfigVar("gui.workspace.lastSavePath");
		lastOpenPath = framework.getConfigVar("gui.workspace.lastOpenPath");
	}

	public void shutdown() {
		if (lastSavePath != null)
			framework.setConfigVar("gui.workspace.lastSavePath", lastSavePath);
		if (lastOpenPath != null)
			framework.setConfigVar("gui.workspace.lastOpenPath", lastOpenPath);
	}

	protected int getInsertPoint (DefaultMutableTreeNode node, String caption) {
		if (node.getChildCount() == 0)
			return 0;

		int i;

		for (i=0; i<node.getChildCount(); i++)
			if (node.getChildAt(i).toString().compareToIgnoreCase(caption) > 0)
				return i;

		return i;

	}

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

				if (i > 0 &&  i < s.length() - 1)
					folderName = s.substring(i+1).toLowerCase();
			}

			DefaultTreeModel treeModel = (DefaultTreeModel)workspaceTree.getModel();
			DefaultMutableTreeNode folderNode = folderNodes.get(folderName);

			if(folderNode==null) {
				folderNode = new DefaultMutableTreeNode(folderName) ;
				treeModel.insertNodeInto(folderNode, workspaceRoot, getInsertPoint(workspaceRoot, folderName));
				//workspaceRoot.add(folderNode);
				folderNodes.put(folderName, folderNode);
			}

			String folder = null;
			if (we.getModel() != null)
				folder = we.getModel().getDisplayName();

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


	public void workspaceSaved() {
		String title = framework.getWorkspace().getFilePath();
		if (title.isEmpty())
			title = "new workspace";
		title = "(" + title + ")";
		if (framework.getWorkspace().isChanged())
			title = "*" + title;

		workspaceRoot.setUserObject(title);
		repaint();
	}

	public JMenu createMenu() {
		JMenu menu = new JMenu("Workspace");


		ScriptedActionMenuItem miNewModel = new ScriptedActionMenuItem(MainWindow.Actions.CREATE_WORK_ACTION);
		miNewModel.addScriptedActionListener(framework.getMainWindow().getDefaultActionListener());

		ScriptedActionMenuItem miAdd = new ScriptedActionMenuItem(WorkspaceWindow.Actions.ADD_FILES_TO_WORKSPACE_ACTION);
		miAdd.addScriptedActionListener(framework.getMainWindow().getDefaultActionListener());

		ScriptedActionMenuItem miSave = new ScriptedActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_ACTION);
		miSave.addScriptedActionListener(framework.getMainWindow().getDefaultActionListener());

		ScriptedActionMenuItem miSaveAs = new ScriptedActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_AS_ACTION);
		miSaveAs.addScriptedActionListener(framework.getMainWindow().getDefaultActionListener());

		menu.add(miNewModel);
		menu.addSeparator();
		menu.add(miAdd);
		menu.add(miSave);
		menu.add(miSaveAs);

		return menu;
	}

	public void addToWorkspace() {
		JFileChooser fc;
		if (lastOpenPath != null)
			fc = new JFileChooser(lastOpenPath);
		else
			fc = new JFileChooser();

		fc.setDialogTitle("Add files to workspace");
		fc.setMultiSelectionEnabled(true);
		fc.addChoosableFileFilter(FileFilters.DOCUMENT_FILES);
		fc.setFileFilter(fc.getAcceptAllFileFilter());
		if(fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
			for(File file : fc.getSelectedFiles())
				try {
					framework.getWorkspace().add(file.getPath(), false);
				} catch (DeserialisationException e) {
					JOptionPane.showMessageDialog(null, "The file \"" + file.getName() + "\" could not be loaded. Please refer to the Problems window for details.", "Load error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}

				lastOpenPath = fc.getCurrentDirectory().getPath();
		}
	}

	public void saveWorkspace() throws OperationCancelledException {
		if (framework.getWorkspace().getFilePath() != null)
			framework.getWorkspace().save(framework.getWorkspace().getFilePath());
		else
			saveWorkspaceAs();
	}

	public void saveWorkspaceAs() throws OperationCancelledException {
		JFileChooser fc;

		if (framework.getWorkspace().getFilePath() != null)
			fc = new JFileChooser(framework.getWorkspace().getFilePath());
		else if (lastSavePath != null)
			fc = new JFileChooser(lastSavePath);
		else
			fc = new JFileChooser();

		fc.setDialogTitle("Save workspace as...");
		fc.setFileFilter(FileFilters.WORKSPACE_FILES);

		String path;

		while (true) {
			if(fc.showSaveDialog(mainWindow)==JFileChooser.APPROVE_OPTION) {
				path = FileFilters.addExtension(fc.getSelectedFile().getPath(), FileFilters.WORKSPACE_EXTENSION);

				File f = new File(path);

				if (!f.exists())
					break;
				else
					if (JOptionPane.showConfirmDialog(mainWindow, "The file \"" + f.getName()+"\" already exists. Do you want to overwrite it?", "Confirm",
							JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						break;
			} else
				throw new OperationCancelledException("Save operation cancelled by user.");
		}

		framework.getWorkspace().save(path);
		lastSavePath = fc.getCurrentDirectory().getPath();
	}

	private void checkSaved() throws OperationCancelledException {
		if (framework.getWorkspace().isChanged()) {
			int result = JOptionPane.showConfirmDialog(mainWindow, "Current workspace is not saved. Do you wish to save it before opening?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.CANCEL_OPTION)
				throw new OperationCancelledException("Cancelled by user.");

			if (result == JOptionPane.YES_OPTION) {
					mainWindow.closeEditorWindows();
					saveWorkspace();
			} else
					mainWindow.closeEditorWindows();
		}
	}

	public void newWorkspace() {
		try {
			checkSaved();
		} catch (OperationCancelledException e) {
			return;
		}

		framework.getWorkspace().clear();
	}

	public void openWorkspace() {
		try {
			checkSaved();
		} catch (OperationCancelledException e) {
			return;
		}

		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileFilter(FileFilters.WORKSPACE_FILES);

		if (lastOpenPath != null)
			fc.setCurrentDirectory(new File(lastOpenPath));

		fc.setMultiSelectionEnabled(false);
		fc.setDialogTitle("Open workspace");

		if (fc.showDialog(mainWindow, "Open") == JFileChooser.APPROVE_OPTION) {
			try {
				framework.loadWorkspace(fc.getSelectedFile());
			} catch (DeserialisationException e) {
				JOptionPane.showMessageDialog(mainWindow, "Workspace load failed. Please see the Problems window for details.", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}

		lastOpenPath = fc.getCurrentDirectory().getPath();
	}


	public void modelLoaded(WorkspaceEntry we) {
	}

	public void entryChanged(WorkspaceEntry we) {
		repaint();
	}
}
