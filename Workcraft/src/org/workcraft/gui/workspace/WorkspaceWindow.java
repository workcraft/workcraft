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
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.MainWindowActions;
import org.workcraft.gui.WorkspaceTreeDecorator;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;


@SuppressWarnings("serial")
public class WorkspaceWindow extends JPanel {
	public static class Actions {
		public static final Action ADD_FILES_TO_WORKSPACE_ACTION = new Action() {
			@Override public void run(Framework f) {
				f.getMainWindow().getWorkspaceView().addToWorkspace(Path.root(""));
			}
			public String getText() {
				return "Link files to the root of workspace...";
			};
		};

		public static final Action OPEN_WORKSPACE_ACTION = new Action() {
			@Override public void run(Framework f) {
				f.getMainWindow().getWorkspaceView().openWorkspace();
			}
			public String getText() {
				return "Open workspace...";
			};
		};

		public static final Action SAVE_WORKSPACE_ACTION = new Action() {
			@Override public void run(Framework f) {
				try { f.getMainWindow().getWorkspaceView().saveWorkspace(); } catch (OperationCancelledException e) { }
			}
			public String getText() {
				return "Save workspace";
			};
		};
		public static final Action SAVE_WORKSPACE_AS_ACTION = new Action() {
			@Override public void run(Framework f) {
				try { f.getMainWindow().getWorkspaceView().saveWorkspaceAs(); } catch (OperationCancelledException e) { }
			}
			public String getText() {
				return "Save workspace as...";
			};
		};
		public static final Action NEW_WORKSPACE_AS_ACTION = new Action() {
			@Override public void run(Framework f) {
				f.getMainWindow().getWorkspaceView().newWorkspace();
			}
			public String getText() {
				return "New workspace";
			};
		};
	}

	private JScrollPane scrollPane = null;
	private TreeWindow<Path<String>>  workspaceTree = null;

	private final MainWindow mainWindow;
	private final Framework framework;
	private String lastSavePath = null;
	private String lastOpenPath = null;

	public WorkspaceWindow(Framework framework) {
		super();
		this.framework = framework;
		this.mainWindow = framework.getMainWindow();
		startup();
	}

	public void startup() {
		final Workspace workspace = framework.getWorkspace();
		workspaceTree  = TreeWindow.create(workspace.getTree(), new WorkspaceTreeDecorator(workspace), new WorkspacePopupProvider(this));
		scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(8);

		scrollPane.setViewportView(workspaceTree);

		setLayout(new BorderLayout(0,0));
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

		for (i=0;i<node.getChildCount(); i++)
			if (node.getChildAt(i).toString().compareToIgnoreCase(caption) > 0)
				return i;

		return i;
	}

	/*public void entryAdded(WorkspaceEntry we) {
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
			if (we.getObject() instanceof Model)
				folder = ((Model)we.getObject()).getDisplayName();

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
	}*/


	public void workspaceSaved() {
		String title = framework.getWorkspace().getWorkspaceFile().getAbsolutePath();
		if (title.isEmpty())
			title = "new workspace";
		title = "(" + title + ")";
		if (framework.getWorkspace().isChanged())
			title = "*" + title;

		repaint();
	}

	public JMenu createMenu() {
		JMenu menu = new JMenu("Workspace");

		final ScriptedActionListener listener = framework.getMainWindow().getDefaultActionListener();

		ActionMenuItem miNewModel = new ActionMenuItem(MainWindowActions.CREATE_WORK_ACTION);
		miNewModel.addScriptedActionListener(listener);

		ActionMenuItem miAdd = new ActionMenuItem(WorkspaceWindow.Actions.ADD_FILES_TO_WORKSPACE_ACTION);
		miAdd.addScriptedActionListener(listener);

		ActionMenuItem miSave = new ActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_ACTION);
		miSave.addScriptedActionListener(listener);

		ActionMenuItem miSaveAs = new ActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_AS_ACTION);
		miSaveAs.addScriptedActionListener(listener);

		menu.add(miNewModel);
		menu.addSeparator();
		menu.add(miAdd);
		menu.add(miSave);
		menu.add(miSaveAs);

		return menu;
	}

	public void addToWorkspace(Path<String> path) {
		JFileChooser fc;
		if (lastOpenPath != null)
			fc = new JFileChooser(lastOpenPath);
		else
			fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		fc.setDialogTitle("Link to workspace");
		fc.setMultiSelectionEnabled(true);
		fc.addChoosableFileFilter(FileFilters.DOCUMENT_FILES);
		fc.setFileFilter(fc.getAcceptAllFileFilter());
		if(fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
			for(File file : fc.getSelectedFiles())
				framework.getWorkspace().addMount(Path.append(path, file.getName()), file, false);

			lastOpenPath = fc.getCurrentDirectory().getPath();
		}
	}


	public void saveWorkspace() throws OperationCancelledException {
		if (!framework.getWorkspace().isTemporary())
			framework.getWorkspace().save();
		else
			saveWorkspaceAs();
	}

	public void saveWorkspaceAs() throws OperationCancelledException {
		JFileChooser fc;

		if (!framework.getWorkspace().isTemporary())
			fc = new JFileChooser(framework.getWorkspace().getWorkspaceFile());
		else if (lastSavePath != null)
			fc = new JFileChooser(lastSavePath);
		else
			fc = new JFileChooser();

		fc.setDialogTitle("Save workspace as...");
		fc.setFileFilter(FileFilters.WORKSPACE_FILES);

		File file;

		while (true) {
			if(fc.showSaveDialog(mainWindow)==JFileChooser.APPROVE_OPTION) {
				String path = FileFilters.addExtension(fc.getSelectedFile().getPath(), FileFilters.WORKSPACE_EXTENSION);

				file = new File(path);

				if (!file.exists())
					break;
				else
					if (JOptionPane.showConfirmDialog(mainWindow, "The file \"" + file.getName()+"\" already exists. Do you want to overwrite it?", "Confirm",
							JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						break;
			} else
				throw new OperationCancelledException("Save operation cancelled by user.");
		}

		framework.getWorkspace().saveAs(file);
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

	public Workspace getWorkspace() {
		return framework.getWorkspace();
	}
}
