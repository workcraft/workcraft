package org.workcraft.framework.workspace;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.LoadFromXMLException;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class Workspace {
	private LinkedList<WorkspaceEntry> workspace = new LinkedList<WorkspaceEntry>();
	private LinkedList<WorkspaceListener> workspaceListeners = new LinkedList<WorkspaceListener>();

	Framework framework;

	private boolean temporary = true;

	private boolean changed = false;
	private String filePath = null;
	private int entryIDCounter = 0;

	public Workspace(Framework framework) {
		this.framework = framework;
	}

	public WorkspaceEntry add(String path, boolean temporary) throws LoadFromXMLException {
		for(WorkspaceEntry we : workspace)
			if(we.getFile() != null && we.getFile().getPath().equals(path))
				return we;

		File f = new File(path);

		WorkspaceEntry we = null;

		if (f.exists()) {
			we = new WorkspaceEntry(this);
			we.setTemporary(temporary);
			we.setFile(f);
			if (f.getName().endsWith(".work")) {
				Model model = Framework.load(f.getPath());
				we.setModel(model);
			}
			workspace.add(we);
			fireEntryAdded(we);
		}

		return we;
	}

	public WorkspaceEntry add(Model model, boolean temporary) {
		WorkspaceEntry we = new WorkspaceEntry(this);
		we.setTemporary(temporary);
		we.setModel(model);
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

	public Model loadModel(WorkspaceEntry we) throws LoadFromXMLException {
		Model model = Framework.load(we.getFile().getPath());
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

	public void load(String path) throws LoadFromXMLException {
		workspace.clear();

		try {
			Document doc = XmlUtil.loadDocument(path);
			Element xmlroot = doc.getDocumentElement();

			if (xmlroot.getNodeName()!="workcraft-workspace")
				throw new LoadFromXMLException("not a Workcraft workspace file");


			List<Element> entries = XmlUtil.getChildElements("entry", xmlroot);

			for (Element entryElement : entries)
				add(XmlUtil.readStringAttr(entryElement, "path"), false);

			for (WorkspaceEntry we : workspace)
				fireEntryAdded(we);

			filePath = path;

			setTemporary (false);

		} catch (ParserConfigurationException e) {
			throw new LoadFromXMLException (e);
		} catch (SAXException e) {
			throw new LoadFromXMLException (e);
		} catch (IOException e) {
			throw new LoadFromXMLException (e);
		}
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
			if (we.getFile() == null || we.isTemporary())
				continue;
			Element e = doc.createElement("entry");
			e.setAttribute("path", we.getFile().getPath());
			root.appendChild(e);
		}

		try {
			XmlUtil.saveDocument(doc, new File(path));
			filePath = path;
			changed = false;
			fireWorkspaceSaved();

			setTemporary(false);
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private void fireWorkspaceSaved() {
		changed = false;
		for (WorkspaceListener listener : workspaceListeners)
			listener.workspaceSaved();
	}

	void fireModelLoaded(WorkspaceEntry we) {
		for (WorkspaceListener listener : workspaceListeners)
			listener.modelLoaded(we);
	}

	void fireEntryAdded(WorkspaceEntry we) {
		changed = true;
		for (WorkspaceListener listener : workspaceListeners)
			listener.entryAdded(we);
	}

	void fireEntryRemoved(WorkspaceEntry we) {
		changed = true;
		for (WorkspaceListener listener : workspaceListeners)
			listener.entryRemoved(we);
	}

	void fireEntryChanged(WorkspaceEntry we) {
		changed = true;
		for (WorkspaceListener listener : workspaceListeners)
			listener.entryChanged(we);
	}

	public int getNextEntryID() {
		return entryIDCounter++;
	}

	public void clear() {
		changed = false;
		filePath = null;
		entryIDCounter = 0;

		for (WorkspaceEntry we : workspace)
			remove(we);
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}

}