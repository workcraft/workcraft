package org.workcraft.framework.workspace;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.util.XmlUtil;

public class Workspace {
	LinkedList<File> workspace = new LinkedList<File>();
	LinkedList<AbstractGraphModel> workbench = new LinkedList<AbstractGraphModel>();

	LinkedList<WorkspaceListener> workspaceListeners = new LinkedList<WorkspaceListener>();
	LinkedList<WorkbenchListener> workbenchListeners = new LinkedList<WorkbenchListener>();

	Framework framework;

	private boolean changed = false;
	private String filePath = "";


	public Workspace(Framework framework) {
		this.framework = framework;
	}

	public File add(String path) {
		for(File f : workspace) {
			if(f.getPath().equals(path))
				return f;
		}
		File f = new File(path);
		workspace.add(f);
		fireWorkspaceChanged();
		return f;
	}

	public void remove(File f) {
		workspace.remove(f);
		fireWorkspaceChanged();
	}

	public List<File> entries() {
		return Collections.unmodifiableList(workspace);
	}

	public AbstractGraphModel loadDocument(File f) throws ModelLoadFailedException {
		AbstractGraphModel doc = framework.load(f.getPath());
		fireDocumentLoaded(doc);
		return doc;
	}



	public void addListener (WorkspaceListener l) {
		workspaceListeners.add(l);
	}

	public String getFilePath() {
		return filePath;
	}

	public boolean isChanged() {
		return changed;
	}

	public void save(String path) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc;
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		} catch (ParserConfigurationException e) {
			System.err.println(e.getMessage());
			return;
		}

		Element root = doc.createElement("workcraft-workspace");
		doc.appendChild(root);

		for(File f : workspace) {
			Element e = doc.createElement("file");
			e.setAttribute("path", f.getPath());
			root.appendChild(e);
		}

		try {
			XmlUtil.saveDocument(doc, path);
			filePath = path;
			changed = false;
			fireWorkspaceSaved();
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private void fireWorkspaceSaved() {
		for (WorkspaceListener listener : workspaceListeners)
			listener.workspaceSaved();
	}

	void fireDocumentLoaded(AbstractGraphModel doc) {
		for (WorkbenchListener listener : workbenchListeners)
			listener.documentLoaded(doc);
	}

	void fireWorkspaceChanged() {
		for (WorkspaceListener listener : workspaceListeners)
			listener.workspaceChanged();
	}

	public void save() {
		if(filePath.isEmpty()) {
			System.err.println("File name undefined.");
		}
		else
			save(filePath);
	}
}