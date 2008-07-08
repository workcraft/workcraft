package org.workcraft.gui.workspace;

import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.workcraft.framework.Document;
import org.workcraft.framework.Framework;
import org.workcraft.framework.WorkspaceEntry;
import org.workcraft.framework.WorkspaceEventListener;
import org.workcraft.gui.MDIDocumentFrame;


@SuppressWarnings("serial")
public class WorkspaceView extends MDIDocumentFrame implements WorkspaceEventListener {
	private JScrollPane scrollPane = null;
	private JTree workspaceTree = null;

	private Framework framework;
	private DefaultMutableTreeNode workspaceRoot;  //  @jve:decl-index=0:
	private HashMap<String, DefaultMutableTreeNode> folders = new HashMap<String, DefaultMutableTreeNode>();
	private HashMap<WorkspaceEntry, DefaultMutableTreeNode> entries = new HashMap<WorkspaceEntry, DefaultMutableTreeNode>();

	public WorkspaceView(Framework framework) {
		super("Workspace");
		this.framework = framework;
	}

	public void startup() {
		scrollPane = new JScrollPane();

		workspaceRoot = new DefaultMutableTreeNode("(unnamed)");

		workspaceTree = new JTree();
		workspaceTree.setModel(new DefaultTreeModel(workspaceRoot));
		workspaceTree.addMouseListener(new WorkspaceViewPopupListener(framework));

		scrollPane.setViewportView(workspaceTree);

		this.setContentPane(scrollPane);
		this.setTitle("Workspace");

		this.setLocation(framework.getConfigVarAsInt("gui.workspace.x", 0), framework.getConfigVarAsInt("gui.workspace.y", 0));
		this.setSize(framework.getConfigVarAsInt("gui.workspace.width", 500), framework.getConfigVarAsInt("gui.workspace.height", 300));

		workspaceUpdated();
	}

	public void documentOpened(Document doc) {
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
	public void workspaceUpdated() {
		workspaceRoot.setUserObject(framework.getWorkspace().getTitle());

		HashMap<WorkspaceEntry, DefaultMutableTreeNode> newEntries =
			new HashMap<WorkspaceEntry, DefaultMutableTreeNode>();
		for(WorkspaceEntry we : framework.getWorkspace().entries()) {
			DefaultMutableTreeNode node;
			if(entries.containsKey(we)) {
				node = entries.get(we);
			}
			else {
				String folderName;
	//			TODO if(we.isDocument())
				folderName = we.getExtension().toUpperCase();
				DefaultMutableTreeNode folderNode = folders.get(folderName);
				if(folderNode==null) {
					folderNode = new DefaultMutableTreeNode(folderName) ;
					workspaceRoot.add(folderNode);
					folders.put(folderName, folderNode);
				}
				node = new DefaultMutableTreeNode();
				node.setUserObject(we);
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

}
