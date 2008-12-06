package org.workcraft.framework.workspace;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.dom.DisplayName;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.util.XmlUtil;

public class Workspace {
	LinkedList<WorkspaceEntry> workspace = new LinkedList<WorkspaceEntry>();
	LinkedList<WorkspaceListener> workspaceListeners = new LinkedList<WorkspaceListener>();

	Framework framework;

	private boolean changed = false;
	private String filePath = "";

	public Workspace(Framework framework) {
		this.framework = framework;
	}

	public void fillInfo (WorkspaceEntry we, AbstractGraphModel model) {
		Class<?> modelClass = model.getClass();

		we.setModelClassName(modelClass.getName());

		DisplayName displayName = modelClass.getAnnotation(DisplayName.class);
		if (displayName != null)
			we.setModelType(displayName.value());
		else
			we.setModelType(modelClass.getSimpleName());

		we.setModelTitle(model.getTitle());
	}

	public WorkspaceEntry add(String path) throws ModelLoadFailedException {
		for(WorkspaceEntry we : workspace)
			if(we.getFile() != null && we.getFile().getPath().equals(path))
				return we;

		File f = new File(path);

		WorkspaceEntry we = null;

		if (f.exists()) {
			we = new WorkspaceEntry();
			we.setFile(f);
			if (f.getName().endsWith(".work")) {
				AbstractGraphModel model = framework.load(f.getPath());
				fillInfo (we, model);
			}
			workspace.add(we);
			fireEntryAdded(we);
		}

		return we;
	}

	public WorkspaceEntry add(AbstractGraphModel model) {
		WorkspaceEntry we = new WorkspaceEntry();
		we.setModel(model);
		fillInfo (we, model);
		workspace.add(we);
		fireEntryAdded(we);
		return we;
	}

	public void remove(WorkspaceEntry we) {
		workspace.remove(we);
		fireEntryRemoved(we);
	}

	public List<WorkspaceEntry> entries() {
		return Collections.unmodifiableList(workspace);
	}

	public AbstractGraphModel loadModel(WorkspaceEntry we) throws ModelLoadFailedException {
		AbstractGraphModel model = framework.load(we.getFile().getPath());
		fillInfo(we, model);
		fireModelLoaded(we);
		return model;
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

		for(WorkspaceEntry we : workspace) {
			if (we.getFile() == null)
				continue;
			Element e = doc.createElement("entry");
			e.setAttribute("path", we.getFile().getPath());
			if (we.getModelTitle() != null)
				e.setAttribute("title", we.getModelTitle());
			if (we.getModelType() != null)
				e.setAttribute("type", we.getModelType());
			if (we.getModelClassName() != null)
				e.setAttribute("class", we.getModelClassName());
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

	void fireModelLoaded(WorkspaceEntry we) {
		for (WorkspaceListener listener : workspaceListeners)
			listener.modelLoaded(we);
	}

	void fireEntryAdded(WorkspaceEntry we) {
		for (WorkspaceListener listener : workspaceListeners)
			listener.entryAdded(we);
	}

	void fireEntryRemoved(WorkspaceEntry we) {
		for (WorkspaceListener listener : workspaceListeners)
			listener.entryRemoved(we);
	}

	public void save() {
		if(filePath.isEmpty()) {
			System.err.println("File name undefined.");
		}
		else
			save(filePath);
	}
}