package org.workcraft.gui.workspace;

import java.io.File;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.framework.Framework;
import org.workcraft.framework.workspace.WorkspaceListener;
import org.workcraft.gui.InternalWindow;


@SuppressWarnings("serial")
public class WorkspaceWindow extends InternalWindow implements WorkspaceListener {
	private JScrollPane scrollPane = null;
	private JTree workspaceTree = null;

	private Framework framework;

	private DefaultMutableTreeNode workspaceRoot;
	private HashMap<String, DefaultMutableTreeNode> folders = new HashMap<String, DefaultMutableTreeNode>();
	private HashMap<File, DefaultMutableTreeNode> entries = new HashMap<File, DefaultMutableTreeNode>();

	public WorkspaceWindow(Framework framework) {
		super("Workspace");
		this.framework = framework;
	}

	public void startup() {
		scrollPane = new JScrollPane();

		workspaceRoot = new DefaultMutableTreeNode("[new workspace]");

		workspaceTree = new JTree();
		workspaceTree.setModel(new DefaultTreeModel(workspaceRoot));
		workspaceTree.addMouseListener(new WorkspaceWindowPopupListener(framework));

		scrollPane.setViewportView(workspaceTree);

		this.setContentPane(scrollPane);
		this.setTitle("Workspace");

		this.setLocation(framework.getConfigVarAsInt("gui.workspace.x", 0), framework.getConfigVarAsInt("gui.workspace.y", 0));
		this.setSize(framework.getConfigVarAsInt("gui.workspace.width", 500), framework.getConfigVarAsInt("gui.workspace.height", 300));

		workspaceChanged();
	}

	public void documentOpened(AbstractGraphModel doc) {
/*		Class<?> model = doc.getClass();
		DefaultMutableTreeNode folder, docNode;

		if (classFolders.containsKey(model)) {
			folder = classFolders.get(model);
		} else {
			folder = new DefaultMutableTreeNode(ModelManager.getModelDisplayName(model)) ;
			workspaceRoot.add(folder);
			classFolders.put(model, folder);
		}
		docNode = new DefaultMutableTreeNode ( new TreeViewDocumentWrapper(doc));
		folder.add(docNode);
		//getWorkspaceTree().expandRow(0);

		workspaceTree.makeVisible(new TreePath(new Object[] {workspaceRoot, folder, docNode}));
		workspaceTree.updateUI();*/
	}

	public void shutdown() {
		framework.setConfigVar("gui.workspace.x", this.getX());
		framework.setConfigVar("gui.workspace.y", this.getY());
		framework.setConfigVar("gui.workspace.width", this.getWidth());
		framework.setConfigVar("gui.workspace.height", this.getHeight());
	}

	@Override
	public void workspaceChanged() {
		String title = framework.getWorkspace().getFilePath();
		if (title.isEmpty())
			title = "new workspace";
		title = "[" + title + "]";
		if (framework.getWorkspace().isChanged())
			title = "*" + title;

		workspaceRoot.setUserObject(title);

		HashMap<File, DefaultMutableTreeNode> newEntries =
			new HashMap<File, DefaultMutableTreeNode>();

		for(File we : framework.getWorkspace().entries()) {
			DefaultMutableTreeNode node;
			if(entries.containsKey(we)) {
				node = entries.get(we);
			}
			else {
				String folderName = "";
		        String s = we.getName();
		        int i = s.lastIndexOf('.');

		        if (i > 0 &&  i < s.length() - 1) {
		            folderName = s.substring(i+1).toLowerCase();
		        }
				DefaultMutableTreeNode folderNode = folders.get(folderName);
				if(folderNode==null) {
					folderNode = new DefaultMutableTreeNode(folderName) ;
					workspaceRoot.add(folderNode);
					folders.put(folderName, folderNode);
				}
				node = new DefaultMutableTreeNode();
				node.setUserObject(new WorkspaceEntry(we));
				folderNode.add(node);
			}
			newEntries.put(we, node);

			if(entries!=null)
				entries.remove(we);
		}
		if(entries!=null) {
			for(DefaultMutableTreeNode node : entries.values()) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
				parent.remove(node);
				if(parent.getChildCount()==0) {
					folders.remove(parent.toString());
					workspaceRoot.remove(parent);
				}
			}
		}
		workspaceTree.updateUI();
		entries = newEntries;
	}

	@Override
	public void workspaceSaved() {
		String title = framework.getWorkspace().getFilePath();
		if (title.isEmpty())
			title = "new workspace";
		title = "[" + title + "]";
		if (framework.getWorkspace().isChanged())
			title = "*" + title;

		workspaceRoot.setUserObject(title);
	}

}
